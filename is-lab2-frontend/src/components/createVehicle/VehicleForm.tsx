import React from 'react';
import {
  Box,
  Button,
  ButtonGroup,
  Flex,
  Input,
  Text,
  Portal,
  Select,
} from '@chakra-ui/react';
import type { CoordinateMode, FormErrors, FormState } from './vehicleFormTypes';
import type { FuelType, VehicleType } from '../../api/models/vehicles';
import { fuelTypeCollection, vehicleTypeCollection } from '../collections';

type Props = {
  form: FormState;
  errors: FormErrors;
  coordinateMode: CoordinateMode;
  selectedCoordId: number | null;
  submitting: boolean;
  onFieldChange: <K extends keyof FormState>(
    field: K,
    value: FormState[K]
  ) => void;
  onCoordinateModeChange: (mode: CoordinateMode) => void;
  onSubmit: (e: React.FormEvent) => void;
  onCancel: () => void;
};

const VehicleForm = ({
  form,
  errors,
  coordinateMode,
  selectedCoordId,
  submitting,
  onFieldChange,
  onCoordinateModeChange,
  onSubmit,
  onCancel,
}: Props) => {
  const showRightPanel = coordinateMode === 'existing';

  return (
    <Box
      as="form"
      onSubmit={onSubmit}
      borderWidth="1px"
      borderRadius="md"
      p={4}
      w="100%"
      maxW={showRightPanel ? '520px' : '480px'}
      mx={showRightPanel ? 0 : 'auto'}
    >
      <Flex direction="column" gap={4}>
        <Box>
          <Text mb={1} fontWeight="medium">
            Название
          </Text>
          <Input
            size="sm"
            value={form.name}
            onChange={(e) => onFieldChange('name', e.target.value)}
            placeholder="Введите название"
          />
          {errors.name && (
            <Text mt={1} fontSize="sm" color="red.500">
              {errors.name}
            </Text>
          )}
        </Box>

        <Box>
          <Text mb={1} fontWeight="medium">
            Тип ТС (необязательно)
          </Text>
          <Select.Root
            collection={vehicleTypeCollection}
            size="sm"
            width="100%"
            value={form.type ? [form.type] : []}
            onValueChange={(details: any) => {
              const v = details.value?.[0] as VehicleType | undefined;
              onFieldChange('type', (v ?? '') as FormState['type']);
            }}
          >
            <Select.HiddenSelect />
            <Select.Control>
              <Select.Trigger>
                <Select.ValueText placeholder="Выберите тип" />
              </Select.Trigger>
              <Select.IndicatorGroup>
                <Select.Indicator />
              </Select.IndicatorGroup>
            </Select.Control>
            <Portal>
              <Select.Positioner>
                <Select.Content>
                  {vehicleTypeCollection.items.map((item) => (
                    <Select.Item item={item} key={item.value}>
                      {item.label}
                      <Select.ItemIndicator />
                    </Select.Item>
                  ))}
                </Select.Content>
              </Select.Positioner>
            </Portal>
          </Select.Root>
        </Box>

        <Flex gap={4} flexWrap="wrap">
          <Box flex="1" minW="160px">
            <Text mb={1} fontWeight="medium">
              Мощность двигателя
            </Text>
            <Input
              size="sm"
              type="number"
              value={form.enginePower}
              onChange={(e) => onFieldChange('enginePower', e.target.value)}
              placeholder="≥ 1"
            />
            {errors.enginePower && (
              <Text mt={1} fontSize="sm" color="red.500">
                {errors.enginePower}
              </Text>
            )}
          </Box>

          <Box flex="1" minW="160px">
            <Text mb={1} fontWeight="medium">
              Количество колёс
            </Text>
            <Input
              size="sm"
              type="number"
              value={form.numberOfWheels}
              onChange={(e) => onFieldChange('numberOfWheels', e.target.value)}
              placeholder="≥ 1"
            />
            {errors.numberOfWheels && (
              <Text mt={1} fontSize="sm" color="red.500">
                {errors.numberOfWheels}
              </Text>
            )}
          </Box>
        </Flex>

        <Flex gap={4} flexWrap="wrap">
          <Box flex="1" minW="160px">
            <Text mb={1} fontWeight="medium">
              Вместимость
            </Text>
            <Input
              size="sm"
              type="number"
              value={form.capacity}
              onChange={(e) => onFieldChange('capacity', e.target.value)}
              placeholder="≥ 1"
            />
            {errors.capacity && (
              <Text mt={1} fontSize="sm" color="red.500">
                {errors.capacity}
              </Text>
            )}
          </Box>

          <Box flex="1" minW="160px">
            <Text mb={1} fontWeight="medium">
              Пробег
            </Text>
            <Input
              size="sm"
              type="number"
              value={form.distanceTravelled}
              onChange={(e) =>
                onFieldChange('distanceTravelled', e.target.value)
              }
              placeholder="≥ 1"
            />
            {errors.distanceTravelled && (
              <Text mt={1} fontSize="sm" color="red.500">
                {errors.distanceTravelled}
              </Text>
            )}
          </Box>
        </Flex>

        <Flex gap={4} flexWrap="wrap">
          <Box flex="1" minW="160px">
            <Text mb={1} fontWeight="medium">
              Расход топлива
            </Text>
            <Input
              size="sm"
              type="number"
              value={form.fuelConsumption}
              onChange={(e) => onFieldChange('fuelConsumption', e.target.value)}
              placeholder="≥ 1"
            />
            {errors.fuelConsumption && (
              <Text mt={1} fontSize="sm" color="red.500">
                {errors.fuelConsumption}
              </Text>
            )}
          </Box>

          <Box flex="1" minW="200px">
            <Text mb={1} fontWeight="medium">
              Тип топлива
            </Text>
            <Select.Root
              collection={fuelTypeCollection}
              size="sm"
              width="100%"
              value={form.fuelType ? [form.fuelType] : []}
              onValueChange={(details: any) => {
                const v = details.value?.[0] as FuelType | undefined;
                onFieldChange('fuelType', (v ?? '') as FormState['fuelType']);
              }}
            >
              <Select.HiddenSelect />
              <Select.Control>
                <Select.Trigger>
                  <Select.ValueText placeholder="Выберите тип топлива" />
                </Select.Trigger>
                <Select.IndicatorGroup>
                  <Select.Indicator />
                </Select.IndicatorGroup>
              </Select.Control>
              <Portal>
                <Select.Positioner>
                  <Select.Content>
                    {fuelTypeCollection.items.map((item) => (
                      <Select.Item item={item} key={item.value}>
                        {item.label}
                        <Select.ItemIndicator />
                      </Select.Item>
                    ))}
                  </Select.Content>
                </Select.Positioner>
              </Portal>
            </Select.Root>
            {errors.fuelType && (
              <Text mt={1} fontSize="sm" color="red.500">
                {errors.fuelType}
              </Text>
            )}
          </Box>
        </Flex>

        <Box>
          <Flex justify="space-between" align="center" mb={2}>
            <Text fontWeight="medium">Координаты</Text>
            <ButtonGroup size="xs" variant="outline" gap={2}>
              <Button
                variant={coordinateMode === 'new' ? 'solid' : 'outline'}
                onClick={() => onCoordinateModeChange('new')}
              >
                Новая
              </Button>
              <Button
                variant={coordinateMode === 'existing' ? 'solid' : 'outline'}
                onClick={() => onCoordinateModeChange('existing')}
              >
                Из списка
              </Button>
            </ButtonGroup>
          </Flex>

          {coordinateMode === 'none' && (
            <Text fontSize="sm" color="gray.500">
              Выберите, создать новую координату или выбрать из существующих.
            </Text>
          )}

          {coordinateMode === 'new' && (
            <Flex gap={4} flexWrap="wrap" mt={2}>
              <Box flex="1" minW="120px">
                <Text mb={1}>X</Text>
                <Input
                  size="sm"
                  type="number"
                  value={form.coordX}
                  onChange={(e) => onFieldChange('coordX', e.target.value)}
                />
                {errors.coordX && (
                  <Text mt={1} fontSize="sm" color="red.500">
                    {errors.coordX}
                  </Text>
                )}
              </Box>
              <Box flex="1" minW="120px">
                <Text mb={1}>Y (≤ 910)</Text>
                <Input
                  size="sm"
                  type="number"
                  value={form.coordY}
                  onChange={(e) => onFieldChange('coordY', e.target.value)}
                />
                {errors.coordY && (
                  <Text mt={1} fontSize="sm" color="red.500">
                    {errors.coordY}
                  </Text>
                )}
              </Box>
            </Flex>
          )}

          {coordinateMode === 'existing' && (
            <Box mt={2}>
              {selectedCoordId ? (
                <Text fontSize="sm" color="gray.600">
                  Выбрана координата ID {selectedCoordId}
                </Text>
              ) : (
                <Text fontSize="sm" color="gray.500">
                  Выберите координату из списка справа.
                </Text>
              )}
              {errors.coordChoice && (
                <Text mt={1} fontSize="sm" color="red.500">
                  {errors.coordChoice}
                </Text>
              )}
            </Box>
          )}

          {errors.coordMode && (
            <Text mt={1} fontSize="sm" color="red.500">
              {errors.coordMode}
            </Text>
          )}
        </Box>

        {errors.common && (
          <Text fontSize="sm" color="red.500">
            {errors.common}
          </Text>
        )}

        <Flex justify="flex-end" gap={3} mt={2}>
          <Button variant="outline" size="sm" type="button" onClick={onCancel}>
            Отмена
          </Button>
          <Button
            colorScheme="teal"
            size="sm"
            type="submit"
            disabled={submitting}
          >
            {submitting ? 'Сохранение...' : 'Сохранить'}
          </Button>
        </Flex>
      </Flex>
    </Box>
  );
};

export default VehicleForm
