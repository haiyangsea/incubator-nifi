package org.apache.nifi.controller.repository;

import org.apache.nifi.datamodel.DataModel;
import org.apache.nifi.datamodel.Model;

/**
 * Created by Allen on 2015/7/11.
 */
public class StandardDataModel implements DataModel {
    private final Model model;

    private StandardDataModel(Builder builder) {
        this.model = builder.model;
    }

    @Override
    public Model getModel() {
        return this.model;
    }

    public static final class Builder {
        private Model model;

        public Builder setModel(Model model) {
            this.model = model;
            return this;
        }

        public Builder fromDataModel(DataModel dataModel) {
            this.model = dataModel.getModel();
            return this;
        }

        public StandardDataModel build() {
            return new StandardDataModel(this);
        }
    }
}
