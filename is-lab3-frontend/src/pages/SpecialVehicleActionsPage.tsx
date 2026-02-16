import { Box, Flex, Text } from '@chakra-ui/react';

import MinEnginePowerSection from '../components/specials/MinEnginePowerSection';
import CountGreaterFuelTypeSection from '../components/specials/CountGreaterFuelTypeSection';
import SearchByNameSection from '../components/specials/SearchByNameSection';
import EnginePowerRangeSection from '../components/specials/EnginePowerRangeSection';
import ResetDistanceSection from '../components/specials/ResetDistanceSection';

const SpecialVehicleActionsPage = () => {

  return (
    <Box p={6}>
      <Box mb={6}>
        <Text fontSize="2xl" fontWeight="bold">
          Специальные действия с транспортными средствами
        </Text>
      </Box>

      <Flex direction="column" gap={6}>
        <MinEnginePowerSection />
        <CountGreaterFuelTypeSection />
        <SearchByNameSection />
        <EnginePowerRangeSection />
        <ResetDistanceSection />
      </Flex>
    </Box>
  );
};

export default SpecialVehicleActionsPage;
