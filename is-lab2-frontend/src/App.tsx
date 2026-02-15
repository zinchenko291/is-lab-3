import { BrowserRouter, Navigate, Route, Routes } from 'react-router';
import Main from './pages/MainPage';
import Layout from './components/layouts/Layout.tsx';
import CoordinatesPage from './pages/coordinate/CoordinatesPage.tsx';
import CoordinateCreatePage from './pages/coordinate/CoordinateCreatePage.tsx';
import Providers from './components/Providers';
import CoordinateViewPage from './pages/coordinate/CoordinateViewPage.tsx';
import CoordinateEditPage from './pages/coordinate/CoordinateEditPage.tsx';
import VehicleCreatePage from './pages/vehicle/VehicleCreatePage.tsx';
import VehicleViewPage from './pages/vehicle/VehicleViewPage.tsx';
import EditVehiclePage from './pages/vehicle/VehicleEditPage.tsx';
import SpecialVehicleActionsPage from './pages/SpecialVehicleActionsPage';
import SignInPage from './pages/auth/SignInPage.tsx';
import RegisterPage from './pages/auth/RegisterPage.tsx';
import ImportsPage from './pages/imports/ImportsPage.tsx';
import ImportConflictsPage from './pages/imports/ImportConflictsPage.tsx';

const App = () => {
  return (
    <Providers>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route path="*" element={<Navigate to={'/'} />} />
            <Route index element={<Main />} />
            <Route path="/coordinates" element={<CoordinatesPage />} />
            <Route
              path="/coordinates/create"
              element={<CoordinateCreatePage />}
            />
            <Route path="/coordinates/:id" element={<CoordinateViewPage />} />
            <Route
              path="/coordinates/:id/edit"
              element={<CoordinateEditPage />}
            />
            <Route path="/vehicles/create" element={<VehicleCreatePage />} />
            <Route path="/vehicles/:id" element={<VehicleViewPage />} />
            <Route path="/vehicles/:id/edit" element={<EditVehiclePage />} />
            <Route path="/specials" element={<SpecialVehicleActionsPage />} />
            <Route path="/imports" element={<ImportsPage />} />
            <Route
              path="/imports/:id/conflicts"
              element={<ImportConflictsPage />}
            />
            <Route path="/auth" element={<SignInPage />} />
            <Route path="/auth/register" element={<RegisterPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </Providers>
  );
};

export default App;
