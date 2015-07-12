package org.apache.nifi.datamodel;

import java.util.List;

/**
 * Created by Allen on 2015/7/11.
 */
public interface DataModelExtension {
    DataModel getDataModel();

    List<DataModel> getDataModels();
}
