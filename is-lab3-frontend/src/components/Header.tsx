import { Button, HStack } from '@chakra-ui/react';
import { Link, useLocation, useNavigate } from 'react-router';
import { ColorModeButton } from './ui/color-mode';
import { AuthService } from '../api/services/authService.ts';
import CacheStats from './CacheStats.tsx';

const Header = () => {
  const location = useLocation();
  const navigate = useNavigate();

  if (location.pathname.includes('/auth')) return null;

  const handleLogout = async () => {
    try {
      await AuthService.logout();
    } catch (error) {
      console.error(error);
    } finally {
      navigate('/auth');
    }
  };

  return (
    <HStack
      m={2}
      py={2}
      px={4}
      justifyContent={'space-between'}
      bgColor={'bg.subtle'}
      borderRadius={4}
      borderColor={'border.emphasized'}
      borderWidth={1}
    >
      <HStack gap={3}>
        <Link style={{ textDecoration: 'underline' }} to="/">
          Транспортные средства
        </Link>
        <Link style={{ textDecoration: 'underline' }} to="/coordinates">
          Координаты
        </Link>
        <Link style={{ textDecoration: 'underline' }} to="/specials">
          Специальные действия
        </Link>
        <Link style={{ textDecoration: 'underline' }} to="/imports">
          Импорт
        </Link>
      </HStack>
      <HStack gap={2}>
        <CacheStats />
        <ColorModeButton />
        <Button variant={'outline'} size="sm" onClick={handleLogout}>
          Выйти
        </Button>
      </HStack>
    </HStack>
  );
};

export default Header;
