import dayjs from 'dayjs';
import {
  HStack,
  Input,
  Portal,
  Select,
} from '@chakra-ui/react';
import { fuelTypeCollection, vehicleTypeCollection } from './collections';
import type { VehicleField } from '../api/models/vehicles';

export type FieldKind = 'string' | 'number' | 'enum' | 'datetime';

type FieldMeta = {
  value: string;
  label: string;
  kind: FieldKind;
  enumType?: 'type' | 'fuelType';
};

export const fieldMeta: {
  value: VehicleField;
  label: string;
  kind: FieldKind;
  enumType?: 'type' | 'fuelType';
}[] = [
  { value: 'id', label: 'ID', kind: 'number' },
  { value: 'name', label: 'Название', kind: 'string' },
  { value: 'creationDate', label: 'Дата создания', kind: 'datetime' },
  { value: 'enginePower', label: 'Мощность двигателя', kind: 'number' },
  { value: 'numberOfWheels', label: 'Количество колёс', kind: 'number' },
  { value: 'distanceTravelled', label: 'Пробег', kind: 'number' },
  { value: 'type', label: 'Тип ТС', kind: 'enum', enumType: 'type' },
  {
    value: 'fuelType',
    label: 'Тип топлива',
    kind: 'enum',
    enumType: 'fuelType',
  },
];

type Props = {
  meta?: FieldMeta;
  value: string;
  onChange: (value: string) => void;
};

const FilterValueControl = ({ meta, value, onChange }: Props) => {
  if (!meta) {
    return (
      <Input
        size="sm"
        placeholder="Сначала выберите поле"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        disabled
      />
    );
  }

  if (meta.kind === 'enum') {
    const collection =
      meta.enumType === 'type'
        ? vehicleTypeCollection
        : fuelTypeCollection;

    return (
      <Select.Root
        collection={collection}
        size="sm"
        width="100%"
        value={value ? [value] : []}
        onValueChange={(details: any) => {
          const v = details.value?.[0] as string | undefined;
          onChange(v ?? '');
        }}
      >
        <Select.HiddenSelect />
        <Select.Control>
          <Select.Trigger>
            <Select.ValueText placeholder="Выберите значение" />
          </Select.Trigger>
          <Select.IndicatorGroup>
            <Select.Indicator />
          </Select.IndicatorGroup>
        </Select.Control>
        <Portal>
          <Select.Positioner>
            <Select.Content>
              {collection.items.map((item) => (
                <Select.Item item={item} key={item.value}>
                  {item.label}
                  <Select.ItemIndicator />
                </Select.Item>
              ))}
            </Select.Content>
          </Select.Positioner>
        </Portal>
      </Select.Root>
    );
  }

  if (meta.kind === 'datetime') {
    const dt = value ? dayjs(value) : null;

    const datePart =
      dt && dt.isValid() ? dt.format('YYYY-MM-DD') : '';
    const timePart =
      dt && dt.isValid() ? dt.format('HH:mm') : '';

    const handleDateChange = (newDate: string) => {
      if (!newDate && !timePart) {
        onChange('');
        return;
      }

      const base = dayjs(
        `${newDate || datePart || dayjs().format('YYYY-MM-DD')}T${
          timePart || '00:00'
        }`
      );

      onChange(base.isValid() ? base.toDate().toISOString() : '');
    };

    const handleTimeChange = (newTime: string) => {
      if (!newTime && !datePart) {
        onChange('');
        return;
      }

      const base = dayjs(
        `${datePart || dayjs().format('YYYY-MM-DD')}T${
          newTime || timePart || '00:00'
        }`
      );

      onChange(base.isValid() ? base.toDate().toISOString() : '');
    };

    return (
      <HStack gap={2}>
        <Input
          size="sm"
          type="date"
          value={datePart}
          onChange={(e) => handleDateChange(e.target.value)}
        />
        <Input
          size="sm"
          type="time"
          value={timePart}
          onChange={(e) => handleTimeChange(e.target.value)}
        />
      </HStack>
    );
  }

  const isNumber = meta.kind === 'number';

  return (
    <Input
      size="sm"
      type={isNumber ? 'number' : 'text'}
      placeholder={isNumber ? 'Введите число' : 'Введите значение'}
      value={value}
      onChange={(e) => onChange(e.target.value)}
    />
  );
};

export default FilterValueControl;
