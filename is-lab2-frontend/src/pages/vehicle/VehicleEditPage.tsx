import React, { useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { useNavigate, useParams } from 'react-router';
import { Box, Flex, Spinner, Text, Button } from '@chakra-ui/react';

import { useStores } from '../../store';
import type { VehicleDto, FuelType } from '../../api/models/vehicles.ts';
import type {
  CoordinateMode,
  FormErrors,
  FormState,
} from '../../components/createVehicle/vehicleFormTypes.ts';
import VehicleForm from '../../components/createVehicle/VehicleForm.tsx';
import CoordinatesListPanel from '../../components/createVehicle/CoordinatesListPanel.tsx';
import validateVehicleForm from '../../api/validators/validateVehicleForm.ts';
import { toaster } from '../../components/ui/toaster.tsx';
import { getApiErrorMessage } from '../../api/getApiErrorMessage';

const EditVehiclePage = observer(() => {
  const { vehiclesStore, coordinatesStore } = useStores();
  const navigate = useNavigate();
  const params = useParams<{ id: string }>();

  const id = Number(params.id);
  const isInvalidId = !params.id || Number.isNaN(id);

  const vehicle = !isInvalidId ? vehiclesStore.getById(id) : null;
  const { loading: vehiclesLoading, error: vehiclesError } = vehiclesStore;

  const [form, setForm] = useState<FormState>({
    name: '',
    type: '',
    enginePower: '',
    numberOfWheels: '',
    capacity: '',
    distanceTravelled: '',
    fuelConsumption: '',
    fuelType: '',
    coordX: '',
    coordY: '',
  });

  const [errors, setErrors] = useState<FormErrors>({});
  const [coordinateMode, setCoordinateMode] =
    useState<CoordinateMode>('none');
  const [selectedCoordId, setSelectedCoordId] = useState<number | null>(
    null
  );
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (isInvalidId) return;

    if (!vehicle) {
      vehiclesStore.fetchById(id).catch(() => {});
    }
  }, [isInvalidId, id, vehicle, vehiclesStore]);

  useEffect(() => {
    if (vehicle) {
      setForm({
        name: vehicle.name ?? '',
        type: vehicle.type ?? '',
        enginePower:
          vehicle.enginePower != null ? String(vehicle.enginePower) : '',
        numberOfWheels:
          vehicle.numberOfWheels != null
            ? String(vehicle.numberOfWheels)
            : '',
        capacity:
          vehicle.capacity != null ? String(vehicle.capacity) : '',
        distanceTravelled:
          vehicle.distanceTravelled != null
            ? String(vehicle.distanceTravelled)
            : '',
        fuelConsumption:
          vehicle.fuelConsumption != null
            ? String(vehicle.fuelConsumption)
            : '',
        fuelType: vehicle.fuelType ?? '',
        coordX: '',
        coordY: '',
      });

      if (vehicle.coordinates) {
        setCoordinateMode('existing');
        setSelectedCoordId(vehicle.coordinates.id);
      } else {
        setCoordinateMode('none');
        setSelectedCoordId(null);
      }
    }
  }, [vehicle]);

  useEffect(() => {
    if (coordinateMode === 'existing') {
      coordinatesStore.pageSize = 10;
      if (coordinatesStore.coordinates.length === 0) {
        coordinatesStore.loadCoordinates(0);
      }
    }
  }, [coordinateMode, coordinatesStore]);

  const handleFieldChange = <K extends keyof FormState>(
    field: K,
    value: FormState[K]
  ) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({
      ...prev,
      [field]: undefined,
      common: undefined,
    }));
  };

  const handleCoordinateModeChange = (mode: CoordinateMode) => {
    setCoordinateMode(mode);
    setErrors((prev) => ({
      ...prev,
      coordMode: undefined,
      coordChoice: undefined,
      common: undefined,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!vehicle) {
      setErrors((prev) => ({
        ...prev,
        common: 'Исходные данные транспортного средства не загружены.',
      }));
      return;
    }

    const validationErrors = validateVehicleForm(
      form,
      coordinateMode,
      selectedCoordId
    );
    if (validationErrors) {
      setErrors(validationErrors);
      return;
    }

    try {
      setSubmitting(true);

      let coordinates = vehicle.coordinates;

      if (coordinateMode === 'new') {
        const xNum = Number(form.coordX);
        const yNum = Number(form.coordY);
        const created = await coordinatesStore.createCoordinates({
          x: xNum,
          y: yNum,
        });
        coordinates = created;
      } else if (coordinateMode === 'existing' && selectedCoordId) {
        const fromStore = coordinatesStore.getById(selectedCoordId);
        if (!fromStore) {
          throw new Error('Выбранная координата не найдена');
        }
        coordinates = fromStore;
      } else if (!coordinates) {
        throw new Error('Не выбран способ задания координаты');
      }

      const dto: VehicleDto = {
        ...vehicle,
        name: form.name.trim(),
        coordinates,
        type: (form.type || undefined) as any,
        enginePower: Number(form.enginePower),
        numberOfWheels: Number(form.numberOfWheels),
        capacity: Number(form.capacity),
        distanceTravelled: Number(form.distanceTravelled),
        fuelConsumption: Number(form.fuelConsumption),
        fuelType: form.fuelType as FuelType,
      };

      await vehiclesStore.updateVehicle(vehicle.id, dto);
      toaster.create({
        title: 'Транспортное средство было обновлено',
        type: 'success'
      });
      navigate(`/vehicles/${vehicle.id}`);
    } catch (err: any) {
      const message = await getApiErrorMessage(err);
      setErrors((prev) => ({
        ...prev,
        common:
          message ??
          'Не удалось сохранить транспортное средство',
      }));
    } finally {
      setSubmitting(false);
    }
  };

  const handleCoordsPageChange = (page: number) => {
    coordinatesStore.goToPage(page - 1);
  };

  const coordsTotal = coordinatesStore.total;
  const coordsPageSize = coordinatesStore.pageSize;
  const coordsCurrentPage = coordinatesStore.currentPage + 1;

  const showRightPanel = coordinateMode === 'existing';

  if (isInvalidId) {
    return (
      <Box p={6}>
        <Text mb={4}>
          Некорректный идентификатор транспортного средства.
        </Text>
        <Button
          type="button"
          onClick={() => navigate(-1)}
          variant="outline"
        >
          Назад к списку
        </Button>
      </Box>
    );
  }

  if (vehiclesLoading && !vehicle) {
    return (
      <Box p={6}>
        <Flex justify="center" align="center" gap={3}>
          <Spinner />
          <Text>Загрузка транспортного средства...</Text>
        </Flex>
      </Box>
    );
  }

  if (vehiclesError && !vehicle) {
    return (
      <Box p={6}>
        <Text mb={4}>
          {vehiclesError ??
            'Транспортное средство не найдено или недоступно.'}
        </Text>
        <Button
          type="button"
          onClick={() => navigate(-1)}
          variant="outline"
        >
          Назад к списку
        </Button>
      </Box>
    );
  }

  if (!vehicle) {
    return (
      <Box p={6}>
        <Text mb={4}>
          Транспортное средство не найдено или недоступно.
        </Text>
        <Button
          type="button"
          onClick={() => navigate(-1)}
          variant="outline"
        >
          Назад к списку
        </Button>
      </Box>
    );
  }

  return (
    <Box p={6}>
      <Flex justify="space-between" align="center" mb={6}>
        <Text fontSize="2xl" fontWeight="bold">
          Редактирование транспортного средства #{vehicle.id}
        </Text>
        <Button
          type="button"
          onClick={() => navigate(-1)}
          variant="outline"
        >
          Назад к списку
        </Button>
      </Flex>

      <Flex gap={6} align="flex-start">
        <VehicleForm
          form={form}
          errors={errors}
          coordinateMode={coordinateMode}
          selectedCoordId={selectedCoordId}
          submitting={submitting}
          onFieldChange={handleFieldChange}
          onCoordinateModeChange={handleCoordinateModeChange}
          onSubmit={handleSubmit}
          onCancel={() => navigate(-1)}
        />

        {showRightPanel && (
          <CoordinatesListPanel
            coordinates={coordinatesStore.coordinates}
            loading={coordinatesStore.loading}
            total={coordsTotal}
            pageSize={coordsPageSize}
            currentPage={coordsCurrentPage}
            selectedCoordId={selectedCoordId}
            onSelect={setSelectedCoordId}
            onPageChange={handleCoordsPageChange}
          />
        )}
      </Flex>
    </Box>
  );
});

export default EditVehiclePage;

