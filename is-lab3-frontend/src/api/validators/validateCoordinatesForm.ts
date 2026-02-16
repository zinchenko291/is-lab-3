import type { FormErrors, FormState } from "../../pages/coordinate/CoordinateCreatePage.tsx";
import type { CoordinatesWithoutIdDto } from "../models/coordinates";

const validateCoordinatesForm = (form: FormState): {
  dto?: CoordinatesWithoutIdDto;
  errors?: FormErrors;
} => {
  const newErrors: FormErrors = {};
  const xNum = Number(form.x);
  const yNum = Number(form.y);

  if (!form.x.trim()) {
    newErrors.x = 'X обязательно';
  } else if (Number.isNaN(xNum)) {
    newErrors.x = 'X должно быть числом';
  }

  if (!form.y.trim()) {
    newErrors.y = 'Y обязательно';
  } else if (Number.isNaN(yNum)) {
    newErrors.y = 'Y должно быть числом';
  } else if (yNum > 910) {
    newErrors.y = 'Y не может быть больше 910';
  }

  if (newErrors.x || newErrors.y) {
    return { errors: newErrors };
  }

  return { dto: { x: xNum, y: yNum } };
};

export default validateCoordinatesForm
