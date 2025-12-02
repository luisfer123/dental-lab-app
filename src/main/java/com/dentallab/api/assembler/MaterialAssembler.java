package com.dentallab.api.assembler;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import com.dentallab.api.controller.MaterialController;
import com.dentallab.api.model.MaterialModel;
import com.dentallab.persistence.entity.MaterialEntity;

@Component
public class MaterialAssembler extends RepresentationModelAssemblerSupport<MaterialEntity, MaterialModel> {
	
	public MaterialAssembler() {
		super(MaterialController.class, MaterialModel.class);
	}

	@Override
    public MaterialModel toModel(MaterialEntity e) {
        MaterialModel m = new MaterialModel();

        m.setId(e.getId());
        m.setName(e.getName());
        m.setCategory(e.getCategory());
        m.setUnit(e.getUnit());
        m.setPricePerUnit(e.getPricePerUnit());
        m.setStatus(e.getStatus());
        m.setNotes(e.getNotes());

        return m;
    }

}
