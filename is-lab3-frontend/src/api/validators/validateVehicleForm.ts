import type { CoordinateMode, FormErrors, FormState } from "../../components/createVehicle/vehicleFormTypes";

const validateVehicleForm = (
  form: FormState,
  coordinateMode: CoordinateMode,
  selectedCoordId: number | null
): FormErrors | null => {
  const newErrors: FormErrors = {};

  if (!form.name.trim()) {
    newErrors.name = 'Название обязательно';
  }

  const enginePower = Number(form.enginePower);
  if (!form.enginePower.trim()) {
    newErrors.enginePower = 'Мощность обязательна';
  } else if (Number.isNaN(enginePower)) {
    newErrors.enginePower = 'Мощность должна быть числом';
  } else if (enginePower < 1) {
    newErrors.enginePower = 'Мощность должна быть ≥ 1';
  }

  const numberOfWheels = Number(form.numberOfWheels);
  if (!form.numberOfWheels.trim()) {
    newErrors.numberOfWheels = 'Количество колёс обязательно';
  } else if (Number.isNaN(numberOfWheels)) {
    newErrors.numberOfWheels = 'Количество колёс должно быть числом';
  } else if (numberOfWheels < 1) {
    newErrors.numberOfWheels = 'Количество колёс должно быть ≥ 1';
  }

  const capacity = Number(form.capacity);
  if (!form.capacity.trim()) {
    newErrors.capacity = 'Вместимость обязательна';
  } else if (Number.isNaN(capacity)) {
    newErrors.capacity = 'Вместимость должна быть числом';
  } else if (capacity < 1) {
    newErrors.capacity = 'Вместимость должна быть ≥ 1';
  }

  const distanceTravelled = Number(form.distanceTravelled);
  if (!form.distanceTravelled.trim()) {
    newErrors.distanceTravelled = 'Пробег обязателен';
  } else if (Number.isNaN(distanceTravelled)) {
    newErrors.distanceTravelled = 'Пробег должен быть числом';
  } else if (distanceTravelled < 1) {
    newErrors.distanceTravelled = 'Пробег должен быть ≥ 1';
  }

  const fuelConsumption = Number(form.fuelConsumption);
  if (!form.fuelConsumption.trim()) {
    newErrors.fuelConsumption = 'Расход топлива обязателен';
  } else if (Number.isNaN(fuelConsumption)) {
    newErrors.fuelConsumption = 'Расход должен быть числом';
  } else if (fuelConsumption < 1) {
    newErrors.fuelConsumption = 'Расход должен быть ≥ 1';
  }

  if (!form.fuelType) {
    newErrors.fuelType = 'Тип топлива обязателен';
  }

  if (coordinateMode === 'none') {
    newErrors.coordMode = 'Выберите способ задания координат';
  }

  if (coordinateMode === 'new') {
    const x = Number(form.coordX);
    const y = Number(form.coordY);

    if (!form.coordX.trim()) {
      newErrors.coordX = 'X обязателен';
    } else if (Number.isNaN(x)) {
      newErrors.coordX = 'X должен быть числом';
    }

    if (!form.coordY.trim()) {
      newErrors.coordY = 'Y обязателен';
    } else if (Number.isNaN(y)) {
      newErrors.coordY = 'Y должен быть числом';
    } else if (y > 910) {
      newErrors.coordY = 'Y не может быть больше 910';
    }
  }

  if (coordinateMode === 'existing' && !selectedCoordId) {
    newErrors.coordChoice = 'Выберите координату из списка';
  }

  return Object.keys(newErrors).length > 0 ? newErrors : null;
};

export default validateVehicleForm
