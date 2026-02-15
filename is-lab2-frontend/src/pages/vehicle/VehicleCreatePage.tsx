import React, { useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { useNavigate } from 'react-router';
import { Box, Flex, Text } from '@chakra-ui/react';
import { ArrowLeft } from 'lucide-react';

import { useStores } from '../../store';
import type { FuelType, VehicleWithoutIdDto } from '../../api/models/vehicles.ts';
import type { CoordinateMode, FormErrors, FormState, } from '../../components/createVehicle/vehicleFormTypes.ts';
import validateVehicleForm from '../../api/validators/validateVehicleForm.ts';
import { toaster } from '../../components/ui/toaster.tsx';
import { getApiErrorMessage } from '../../api/getApiErrorMessage';
import VehicleForm from '../../components/createVehicle/VehicleForm.tsx';
import CoordinatesListPanel from '../../components/createVehicle/CoordinatesListPanel.tsx';

const VehicleCreatePage = observer(() => {
  const { vehiclesStore, coordinatesStore } = useStores();
  const navigate = useNavigate();

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
  const [coordinateMode, setCoordinateMode] = useState<CoordinateMode>('none');
  const [selectedCoordId, setSelectedCoordId] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);

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

      let coordinates = null;

      if (coordinateMode === 'new') {
        const xNum = Number(form.coordX);
        const yNum = Number(form.coordY);

        coordinates = await coordinatesStore.createCoordinates({
          x: xNum,
          y: yNum,
        });
      } else if (coordinateMode === 'existing' && selectedCoordId) {
        coordinates = coordinatesStore.getById(selectedCoordId);
        if (!coordinates) {
          throw new Error('Выбранная координата не найдена');
        }
      } else {
        throw new Error('Не выбран способ задания координаты');
      }

      const dto: VehicleWithoutIdDto = {
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

      await vehiclesStore.createVehicle(dto);
      toaster.create({
        title: 'Транспортное средство было создано',
        type: 'success'
      });
      navigate('/');
    } catch (err: any) {
      const message = await getApiErrorMessage(err);
      setErrors((prev) => ({
        ...prev,
        common: message ?? 'Не удалось создать транспортное средство',
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

  return (
    <Box p={6}>
      <Flex justify="space-between" align="center" mb={6}>
        <Text fontSize="2xl" fontWeight="bold">
          Создание транспортного средства
        </Text>
        <button
          type="button"
          onClick={() => navigate('/')}
          style={{ background: 'none', border: 'none', padding: 0 }}
        >
          <Flex align="center" gap={2}>
            <ArrowLeft size={16} />
            <Text>Назад к списку</Text>
          </Flex>
        </button>
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
          onCancel={() => navigate('/')}
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

export default VehicleCreatePage;


