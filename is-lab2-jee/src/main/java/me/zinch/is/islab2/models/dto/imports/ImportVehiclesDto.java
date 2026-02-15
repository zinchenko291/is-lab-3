package me.zinch.is.islab2.models.dto.imports;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "vehicles")
public class ImportVehiclesDto {
    @NotNull
    @Valid
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("vehicles")
    @JsonAlias("vehicle")
    @JacksonXmlProperty(localName = "vehicle")
    private List<ImportVehicleDto> vehicles = new ArrayList<>();

    public List<ImportVehicleDto> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<ImportVehicleDto> vehicles) {
        this.vehicles = vehicles;
    }
}
