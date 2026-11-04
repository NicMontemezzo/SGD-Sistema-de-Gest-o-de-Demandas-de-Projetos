import { ApplicationConfig, importProvidersFrom, provideZoneChangeDetection } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { MdbModalModule } from 'mdb-angular-ui-kit/modal';

import { routes } from './app.routes';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideAnimations(),
    // MdbModalService não é "providedIn: root": sem isto, abrir a tela de demandas dá NullInjectorError
    importProvidersFrom(MdbModalModule),
    provideHttpClient(withInterceptors([jwtInterceptor, errorInterceptor]))
  ]
};
