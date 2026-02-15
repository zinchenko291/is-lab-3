import { Container, Loader } from '@chakra-ui/react';
import { Outlet, useLocation, useNavigate } from 'react-router';
import Header from '../Header.tsx';
import { useEffect, useState } from 'react';
import { AuthService } from '../../api/services/authService.ts';
import { useStores } from '../../store';
import { reaction } from 'mobx';

const Layout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [loading, setLoading] = useState(true);
  const { userStore, webSocketStore } = useStores();

  useEffect(() => {
    if (location.pathname.startsWith('/auth')) {
      setLoading(false);
      return;
    }

    setLoading(true);

    AuthService.validate()
      .then(status => {
        !status && navigate('/auth');
        void userStore.fetchUser();
      })
      .catch(console.error)
      .finally(() => {
        setLoading(false);
      });
  }, []);

  useEffect(() => {
    const dispose = reaction(
      () => webSocketStore.lastImportConflict,
      (conflict) => {
        if (!conflict) return;
        navigate(`/imports/${conflict.importId}/conflicts`);
        webSocketStore.clearLastImportConflict();
      }
    );

    return () => dispose();
  }, [navigate, webSocketStore]);

  if (loading) {
    return <Loader />;
  }

  return (
    <Container>
      <Header />
      <Outlet />
    </Container>
  );
};

export default Layout;
