package com.gillsoft.control.core;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.gillsoft.mapper.model.MapType;
import com.gillsoft.mapper.model.Mapping;
import com.gillsoft.mapper.model.Unmapping;
import com.gillsoft.mapper.service.MappingService;
import com.gillsoft.mapper.service.UnmappingConverter;
import com.gillsoft.model.Address;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Name;
import com.gillsoft.model.Organisation;
import com.gillsoft.model.Segment;
import com.gillsoft.model.Vehicle;
import com.gillsoft.model.request.LangRequest;
import com.gillsoft.model.request.ResourceRequest;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.util.ContextProvider;

public class MappingCreator<T> {
	
	private LangRequest request;
	private Map<String, T> objects;
	private Map<String, T> result;
	private MapType mapType;
	private MapObjectCreator<T> creator;
	private UnmappingCreator<T> unmappingCreator;
	private ObjectIdSetter<T> idSetter;
	
	public LangRequest getRequest() {
		return request;
	}

	public void setRequest(LangRequest request) {
		this.request = request;
	}

	public Map<String, T> getObjects() {
		return objects;
	}

	public void setObjects(Map<String, T> objects) {
		this.objects = objects;
	}

	public Map<String, T> getResult() {
		return result;
	}

	public void setResult(Map<String, T> result) {
		this.result = result;
	}

	public MapType getMapType() {
		return mapType;
	}

	public void setMapType(MapType mapType) {
		this.mapType = mapType;
	}

	public MapObjectCreator<T> getCreator() {
		return creator;
	}

	public void setCreator(MapObjectCreator<T> creator) {
		this.creator = creator;
	}

	public UnmappingCreator<T> getUnmappingCreator() {
		return unmappingCreator;
	}

	public void setUnmappingCreator(UnmappingCreator<T> unmappingCreator) {
		this.unmappingCreator = unmappingCreator;
	}

	public ObjectIdSetter<T> getIdSetter() {
		return idSetter;
	}

	public void setIdSetter(ObjectIdSetter<T> idSetter) {
		this.idSetter = idSetter;
	}

	public static MappingCreator<Locality> localityMappingCreator(LangRequest request,
			Map<String, Locality> objects, Map<String, Locality> result) {
		MappingCreator<Locality> creator = new MappingCreator<>();
		creator.request = request;
		creator.objects = objects;
		creator.result = result;
		creator.mapType = MapType.GEO;
		creator.creator = (mapping, lang, original) -> createLocality(mapping, lang, original);
		creator.unmappingCreator = (original) -> UnmappingConverter.createUnmappingLocality(original);
		creator.idSetter = (resId, id, l) -> l.setId(getKey(resId, id));
		return creator;
	}
	
	/*
	 * из-за того, что не все возвращаемые пункты ресурса смаплены, необходимо
	 * проверять маппинг родителей, если они есть
	 */
	private static Locality createLocality(Mapping mapping, Lang lang, Locality original) {
		Locality locality = DataConverter.createLocality(mapping, lang, original);
		Locality result = locality;
		while ((mapping = mapping.getParent()) != null) {
			locality.setParent(DataConverter.createLocality(mapping, lang, original));
			locality = locality.getParent();
		}
		return result;
	}
	
	public static MappingCreator<Organisation> organisationMappingCreator(LangRequest request,
			Map<String, Organisation> objects, Map<String, Organisation> result) {
		MappingCreator<Organisation> creator = new MappingCreator<>();
		creator.request = request;
		creator.objects = objects;
		creator.result = result;
		creator.mapType = MapType.ORGANIZATION;
		creator.creator = (mapping, lang, original) -> createOrganisation(mapping, lang, original);
		creator.unmappingCreator = (original) -> UnmappingConverter.createUnmappingOrganisation(original);
		creator.idSetter = (resourceId, id, o) -> o.setId(getKey(resourceId, id));
		return creator;
	}
	
	private static Organisation createOrganisation(Mapping mapping, Lang lang, Organisation original) {
		com.gillsoft.ms.entity.Organisation org = getMappedOrganisation(mapping);
		if (org == null) {
			return DataConverter.createOrganisation(mapping, lang, original);
		} else {
			return DataConverter.convert(org);
		}
	}
	
	private static com.gillsoft.ms.entity.Organisation getMappedOrganisation(Mapping mapping) {
		MsDataController dataController = ContextProvider.getBean(MsDataController.class);
		if (dataController != null) {
			return dataController.getMappedOrganisation(mapping.getId());
		} else {
			return null;
		}
	}
	
	public static MappingCreator<Vehicle> vehicleMappingCreator(LangRequest request,
			Map<String, Vehicle> objects, Map<String, Vehicle> result) {
		MappingCreator<Vehicle> creator = new MappingCreator<>();
		creator.request = request;
		creator.objects = objects;
		creator.result = result;
		creator.mapType = MapType.VEHICLE;
		creator.creator = (mapping, lang, original) -> DataConverter.createVehicle(mapping, lang, original);
		creator.unmappingCreator = (original) -> UnmappingConverter.createUnmappingVehicle(original);
		creator.idSetter = (resourceId, id, v) -> v.setId(getKey(resourceId, id));
		return creator;
	}
	
	public static MappingCreator<Segment> segmentMappingCreator(LangRequest request,
			Map<String, Segment> objects, Map<String, Segment> result) {
		MappingCreator<Segment> creator = new MappingCreator<>();
		creator.request = request;
		creator.objects = objects;
		creator.result = result;
		creator.mapType = MapType.TRIP;
		creator.creator = (mapping, lang, original) -> DataConverter.createSegment(mapping, lang, original);
		creator.unmappingCreator = (original) -> UnmappingConverter.createUnmappingSegment(original);
		creator.idSetter = (resourceId, id, s) -> s.setId(getKey(resourceId, id));
		return creator;
	}
	
	/**
	 * Получает мапинг словарей ответа и создает словари из мапинга. Если мапинга нет, то добавляется объект ответа.
	 */
	public void mappingObjects(MappingService mappingService) {
		if (objects == null
				|| objects.isEmpty()) {
			return;
		}
		long resourceId = getResourceId(request);
		long parentResourceId = getParentResourceId(resourceId);
		for (Entry<String, T> object : objects.entrySet()) {
			
			// получаем смапленную сущность
			List<Mapping> mappings = mappingService.getMappings(mapType, parentResourceId, object.getKey(), request.getLang());
			
			// добавляем сущность в мапу под ключем ид ресурса + ид обьекта,
			// чтобы от разных ресурсов не пересекались ид
			if (mappings == null) {
				if (object.getValue() != null) {
					
					// сохраняем несмапленные данные
					Unmapping unmapping = unmappingCreator.create(object.getValue());
					unmapping.setResourceMapId(object.getKey());
					unmapping.setResourceId(parentResourceId);
					unmapping.setType(mapType);
					mappingService.saveUnmapping(unmapping);
					
					T value = object.getValue();
					idSetter.set(resourceId, object.getKey(), value);
					result.put(getKey(resourceId, object.getKey()), value);
					
					// удаляем данные на языках, которые не запрашиваются
					removeUnselectedLang(value);
				}
			} else {
				result.put(getKey(resourceId, object.getKey()), creator.create(mappings.get(0), request.getLang(), object.getValue()));
			}
		}
	}
	
	private long getParentResourceId(long resourceId) {
		MsDataController dataController = ContextProvider.getBean(MsDataController.class);
		if (dataController != null) {
			Resource resource = dataController.getResource(resourceId);
			if (resource != null
					&& resource.getMain() != null) {
				return resource.getMain().getId();
			}
		}
		return resourceId;
	}
	
	public static long getResourceId(ResourceRequest request) {
		return Long.parseLong(request.getParams().getResource().getId());
	}
	
	/**
	 * Ключ словаря с ид ресурса + ид объекта словаря.
	 */
	public static String getKey(long resourceId, String id) {
		return resourceId + ";" + id;
	}
	
	public void removeUnselectedLang(T value) {
		if (request.getLang() != null) {
			if (value instanceof Name) {
				Name name = (Name) value;
				if (name.getName() != null
						&& name.getName().get(request.getLang()) != null) {
					name.getName().keySet().removeIf(l -> l != request.getLang());
				}
			}
			if (value instanceof Address) {
				Address address = (Address) value;
				if (address.getAddress() != null
						&& address.getAddress().get(request.getLang()) != null) {
					address.getAddress().keySet().removeIf(l -> l != request.getLang());
				}
			}
		}
	}

	private interface MapObjectCreator<T> {
		
		public T create(Mapping mapping, Lang lang, T original);
		
	}
	
	private interface ObjectIdSetter<T> {
		
		public void set(long resourceId, String id, T object);
		
	}
	
	private interface UnmappingCreator<T> {
		
		public Unmapping create(T original);
		
	}
	
}
