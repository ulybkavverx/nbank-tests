package api.requests.skelethon.interfaces;

import api.models.BaseModel;

public interface CrudEndpointInterface {
    Object post(BaseModel model);
    Object get(); // получение ресурса без id
    Object update(long id, BaseModel model);
    Object delete(long id);
    Object put(BaseModel model);
}
