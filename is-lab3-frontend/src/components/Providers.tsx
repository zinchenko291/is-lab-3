import type { ReactNode } from 'react';
import { Provider } from './ui/provider';
import { StoreProvider } from '../store';
import { Toaster } from './ui/toaster';

const Providers = ({ children }: { children: ReactNode }) => {
  return (
    <Provider>
      <StoreProvider>
        <Toaster />
        {children}
      </StoreProvider>
    </Provider>
  );
};

export default Providers;
