import {
  Box,
  Flex,
  IconButton,
  Spinner,
  Table,
  Text,
  ButtonGroup,
  Pagination,
} from '@chakra-ui/react';
import { ChevronLeft, ChevronRight, Check } from 'lucide-react';

type Coordinate = {
  id: number;
  x: number;
  y: number;
};

type Props = {
  coordinates: Coordinate[];
  loading: boolean;
  total: number;
  pageSize: number;
  currentPage: number;
  selectedCoordId: number | null;
  onSelect: (id: number) => void;
  onPageChange: (page: number) => void;
};

const CoordinatesListPanel = ({
  coordinates,
  loading,
  total,
  pageSize,
  currentPage,
  selectedCoordId,
  onSelect,
  onPageChange,
}: Props) => {
  const hasPagination = total > pageSize;

  return (
    <Box flex="1" borderWidth="1px" borderRadius="md" p={4}>
      <Flex justify="space-between" align="center" mb={3}>
        <Text fontWeight="semibold">Доступные координаты</Text>
      </Flex>

      {loading && (
        <Flex justify="center" align="center" py={4} gap={2}>
          <Spinner size="sm" />
          <Text>Загрузка...</Text>
        </Flex>
      )}

      {!loading && coordinates.length === 0 && (
        <Text>Координаты отсутствуют.</Text>
      )}

      {coordinates.length > 0 && (
        <>
          <Table.Root size="sm">
            <Table.Header>
              <Table.Row>
                <Table.ColumnHeader>ID</Table.ColumnHeader>
                <Table.ColumnHeader>X</Table.ColumnHeader>
                <Table.ColumnHeader>Y</Table.ColumnHeader>
                <Table.ColumnHeader textAlign="end">Выбор</Table.ColumnHeader>
              </Table.Row>
            </Table.Header>
            <Table.Body>
              {coordinates.map((c) => {
                const isSelected = c.id === selectedCoordId;
                return (
                  <Table.Row
                    key={c.id}
                    onClick={() => onSelect(c.id)}
                    style={{ cursor: 'pointer' }}
                    data-selected={isSelected ? 'true' : 'false'}
                  >
                    <Table.Cell>{c.id}</Table.Cell>
                    <Table.Cell>{c.x}</Table.Cell>
                    <Table.Cell>{c.y}</Table.Cell>
                    <Table.Cell textAlign="end">
                      <IconButton
                        aria-label="Выбрать координату"
                        size="xs"
                        variant={isSelected ? 'solid' : 'ghost'}
                        colorScheme={isSelected ? 'teal' : undefined}
                        onClick={(e) => {
                          e.stopPropagation();
                          onSelect(c.id);
                        }}
                      >
                        <Check size={14} />
                      </IconButton>
                    </Table.Cell>
                  </Table.Row>
                );
              })}
            </Table.Body>
          </Table.Root>

          {hasPagination && (
            <Flex justify="center" mt={4}>
              <Pagination.Root
                count={total}
                pageSize={pageSize}
                page={currentPage}
                onPageChange={(details: any) =>
                  onPageChange(details.page as number)
                }
              >
                <ButtonGroup variant="ghost" size="sm">
                  <Pagination.PrevTrigger asChild>
                    <IconButton aria-label="Предыдущая страница">
                      <ChevronLeft size={16} />
                    </IconButton>
                  </Pagination.PrevTrigger>

                  <Pagination.Items
                    render={(page) => (
                      <IconButton
                        key={page.value}
                        variant={
                          {
                            base: 'ghost',
                            _selected: 'outline',
                          } as any
                        }
                      >
                        {page.value}
                      </IconButton>
                    )}
                  />

                  <Pagination.NextTrigger asChild>
                    <IconButton aria-label="Следующая страница">
                      <ChevronRight size={16} />
                    </IconButton>
                  </Pagination.NextTrigger>
                </ButtonGroup>
              </Pagination.Root>
            </Flex>
          )}
        </>
      )}
    </Box>
  );
};

export default CoordinatesListPanel
