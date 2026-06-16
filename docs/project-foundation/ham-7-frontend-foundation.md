# HAM-7 - Initialize Angular Frontend Project

## What This Issue Was About

This issue created the first version of the MediLink user interface. The frontend is what people will see in the browser, so this sprint focused on making the application structure ready for the patient, doctor, and admin experiences that will come later.

The goal was to build the shell first, then add the real workflows in later issues.

## What Was Added

- An Angular application that can run locally.
- Routing so the app can move between the main areas of the site.
- A home page that explains the project foundation in simple terms.
- Placeholder pages for patient, doctor, and admin areas.
- Environment-based API configuration so the browser app knows where to send backend requests.
- A shared HTTP interceptor that prefixes backend requests with the configured API base URL.
- Shared frontend folders for guards, interceptors, services, components, and models.

## What This Means In Practice

This is the point where the browser app becomes a real scaffold instead of a starter template. The pages already exist, the navigation is in place, and the app is ready to connect to the backend when actual business features are added.

The local API base URL is centralized in the environment files, so the browser app only needs one setting to know where the backend lives.

The current route map is:

- `/` for the overview page
- `/patient` for the patient workspace placeholder
- `/doctor` for the doctor workspace placeholder
- `/admin` for the admin workspace placeholder

## Where To Look

- `medilink-frontend/src/app/app.ts` - root application component
- `medilink-frontend/src/app/app.routes.ts` - routing table
- `medilink-frontend/src/app/app.config.ts` - frontend application setup
- `medilink-frontend/src/app/shared/interceptors/api-base-url.interceptor.ts` - API URL prefixing
- `medilink-frontend/src/environments/environment.ts` - production-style environment values
- `medilink-frontend/src/environments/environment.development.ts` - local development API values
- `medilink-frontend/src/app/features/home/home.page.html` - overview page content
- `medilink-frontend/src/app/features/patient/patient-dashboard.page.ts` - patient placeholder page
- `medilink-frontend/src/app/features/doctor/doctor-dashboard.page.ts` - doctor placeholder page
- `medilink-frontend/src/app/features/admin/admin-dashboard.page.ts` - admin placeholder page

## What Is Still Not Built

- Login and registration screens
- Real patient, doctor, and admin workflows
- Permission checks in the UI
- Data-driven screens that load from backend APIs

This issue prepared the browser app for the next sprint, but the real product flows still need to be built on top of it.
