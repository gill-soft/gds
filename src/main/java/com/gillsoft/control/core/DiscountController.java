package com.gillsoft.control.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import com.gillsoft.commission.Calculator;
import com.gillsoft.model.Commission;
import com.gillsoft.model.Discount;
import com.gillsoft.model.Price;
import com.gillsoft.model.Segment;
import com.gillsoft.model.Trip;
import com.gillsoft.model.TripContainer;
import com.gillsoft.model.response.TripSearchResponse;
import com.gillsoft.ms.entity.ConnectionDiscount;
import com.google.common.base.Objects;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DiscountController {
	
	@Autowired
	private Calculator calculator;
	
	@Autowired
	private MsDataController dataController;
	
	public void applyConnectionDiscount(TripSearchResponse tripSearchResponse) {
		if (tripSearchResponse.getTripContainers() != null) {
			List<ConnectionDiscount> discounts = dataController.getResourceConnectionDiscounts();
			if (discounts != null
					&& !discounts.isEmpty()) {
				
				// чтобы не искать несколько раз одни и те же скидки на повторяющиеся пары ресурсов
				// ключь - ид ресурса "с" + "_" + ид ресурса "на"
				Map<String, List<ConnectionDiscount>> preparedPairDiscounts = new HashMap<>();
				
				// чтобы не просчитывать одни и те же скидки на повторяющиеся сегменты
				// ключь - хэш рейса + "_" + ид скидки
				Map<String, Discount> preparedDiscounts = new HashMap<>();
				for (TripContainer container : tripSearchResponse.getTripContainers()) {
					if (container.getTrips() != null) {
						addDiscount(container.getTrips(), tripSearchResponse.getSegments(), preparedPairDiscounts, preparedDiscounts, discounts, false);
					}
				}
			}
		}
	}
	
	public void applyConnectionDiscount(Trip trip, Map<String, Segment> segments) {
		List<ConnectionDiscount> discounts = dataController.getResourceConnectionDiscounts();
		addDiscount(Collections.singletonList(trip), segments, null, null, discounts, true);
		
		// удаляем скидки, которые приводят стоимость к минусу
		segments.values().forEach(s -> updateDiscounts(s.getPrice()));
	}
	
	private void addDiscount(List<Trip> trips, Map<String, Segment> segments,
			Map<String, List<ConnectionDiscount>> preparedPairDiscounts, Map<String, Discount> preparedDiscounts,
			List<ConnectionDiscount> discounts, boolean updatePrice) {
		for (Trip trip : trips) {
			if (trip.getSegments() != null) {
				for (int i = 0; i < trip.getSegments().size() - 1; i++) {
					Segment curr = segments.get(trip.getSegments().get(i));
					Segment next = segments.get(trip.getSegments().get(i + 1));
					String currId = curr.getResource().getId();
					String nextId = next.getResource().getId();
					List<ConnectionDiscount> pairDiscounts = getConnectionDiscounts(preparedPairDiscounts, currId, nextId, discounts);
					if (!pairDiscounts.isEmpty()) {
						
						// просчитываем скидки (ключь - ид скидки) 
						Map<Long, Discount> discountCommissions = new HashMap<>(pairDiscounts.size());
						for (ConnectionDiscount connectionDiscount : pairDiscounts) {
							Segment discountSegment = connectionDiscount.isApplyToFrom() ? curr : next;
							discountCommissions.put(connectionDiscount.getId(),
									new Discount(getDiscount(connectionDiscount, discountSegment, preparedDiscounts, updatePrice)));
						}
						// проверяем стакаются скидки или нет и выбираем максимальную
						// те, что не стакаются, суммируем и сравниваем с максимальной
						Discount maxDiscount = null;
						List<Discount> stacked = new ArrayList<>();
						for (ConnectionDiscount connectionDiscount : pairDiscounts) {
							if (!connectionDiscount.isStackUp()
									&& (maxDiscount == null || isBetter(maxDiscount, connectionDiscount, discountCommissions))) {
								maxDiscount = discountCommissions.get(connectionDiscount.getId());
							} else {
								stacked.add(discountCommissions.get(connectionDiscount.getId()));
							}
						}
						BigDecimal summ = summ(stacked);
						if (maxDiscount == null
								|| maxDiscount.getValue().compareTo(summ) > 0) {
							trip.setDiscounts(stacked);
						} else {
							trip.setDiscounts(Collections.singletonList(maxDiscount));
						}
					}
				}
				updateDiscounts(segments, trip, preparedDiscounts);
			}
		}
	}
	
	private BigDecimal summ(List<Discount> discounts) {
		BigDecimal summ = BigDecimal.ZERO;
		for (Discount discount : discounts) {
			summ = summ.add(discount.getValue());
		}
		return summ;
	}
	
	/*
	 * Проверяем, чтобы скидка не была больше стоимости.
	 */
	private void updateDiscounts(Map<String, Segment> segments, Trip trip, Map<String, Discount> preparedDiscounts) {
		
		// группируем скидки по рейсам
		Map<String, List<Discount>> groupe = new HashMap<>();
		for (Discount discount : trip.getDiscounts()) {
			for (String id : trip.getSegments()) {
				if (preparedDiscounts.containsKey(segments.get(id).hashCode() + "_" + discount.getId())) {
					if (!groupe.containsKey(id)) {
						groupe.put(id, new ArrayList<>());
					}
					groupe.get(id).add(discount);
				}
			}
		}
		for (Entry<String, List<Discount>> entry : groupe.entrySet()) {
			updateDiscounts(segments.get(entry.getKey()).getPrice().getAmount(),
					summ(entry.getValue()), entry.getValue(), trip.getDiscounts());
		}
	}
	
	/*
	 * Проверяем, чтобы скидка не была больше стоимости. Для варианта когда скидки в стоимости.
	 */
	private void updateDiscounts(Price price) {
		List<Discount> groupeDiscounts = new ArrayList<>(price.getDiscounts());
		updateDiscounts(price.getAmount(), summ(groupeDiscounts), groupeDiscounts, price.getDiscounts());
	}
	
	private void updateDiscounts(BigDecimal price, BigDecimal summ, List<Discount> groupeDiscounts, List<Discount> resultDiscounts) {
		if (summ.abs().compareTo(price) > 0) {
			BigDecimal diff = summ.abs().subtract(price);
			groupeDiscounts.sort((v1, v2) -> v1.getValue().compareTo(v2.getValue()));
			for (Discount discount : groupeDiscounts) {
				if (discount.getValue().abs().compareTo(diff) >= 0) {
					discount.setValue(discount.getValue().add(diff));
					if (discount.getValue().compareTo(BigDecimal.ZERO) == 0) {
						resultDiscounts.remove(discount);
					}
					break;
				} else {
					diff = diff.add(discount.getValue());
					resultDiscounts.remove(discount);
				}
			}
		}
	}
	
	private Discount getDiscount(ConnectionDiscount connectionDiscount, Segment discountSegment,
			Map<String, Discount> preparedDiscounts, boolean updatePrice) {
		String key = discountSegment.hashCode() + "_" + connectionDiscount.getId();
		if (preparedDiscounts != null
				&& preparedDiscounts.containsKey(key)) {
			return preparedDiscounts.get(key);
		}
		Commission commission = dataController.convert(connectionDiscount);
		Price price = updatePrice ? discountSegment.getPrice() : (Price) SerializationUtils.deserialize(SerializationUtils.serialize(discountSegment.getPrice()));
		if (price.getCommissions() == null) {
			price.setCommissions(new ArrayList<>());
		}
		price.getCommissions().add(commission); 
		price = calculator.calculateResource(price, dataController.getUser(), price.getCurrency());
		if (updatePrice) {
			price.setSource(discountSegment.getPrice().getSource());
			discountSegment.setPrice(price);
		}
		for (Commission c : price.getCommissions()) {
			if (Objects.equal(c.getId(), commission.getId())) {
				Discount discount = new Discount(c);
				if (preparedDiscounts != null) {
					preparedDiscounts.put(key, discount);
				}
				return discount;
			}
		}
		return null;
	}
	
	private boolean isBetter(Discount maxDiscount, ConnectionDiscount discount, Map<Long, Discount> discountCommissions) {
		Discount curr = discountCommissions.get(discount.getId());
		return curr.getValue().compareTo(maxDiscount.getValue()) < 0;
	}
	
	private List<ConnectionDiscount> getConnectionDiscounts(Map<String, List<ConnectionDiscount>> preparedPairDiscounts,
			String currId, String nextId, List<ConnectionDiscount> discounts) {
		String key = currId + "_" + nextId;
		if (preparedPairDiscounts != null
				&& preparedPairDiscounts.containsKey(key)) {
			return preparedPairDiscounts.get(key);
		}
		long curr = Long.parseLong(currId);
		long next = Long.parseLong(nextId);
		
		// получаем все подходящие скидки
		List<ConnectionDiscount> pairDiscounts = discounts.stream()
				.filter(d -> d.getFromResource().getId() == curr && d.getToResource().getId() == next).collect(Collectors.toList());
		if (preparedPairDiscounts != null) {
			preparedPairDiscounts.put(key, pairDiscounts);
		}
		return pairDiscounts;
	}

}
