# Étape 1 : Build de l'application Angular
FROM node:24-alpine AS build
WORKDIR /app

# Installation des dépendances
COPY codebase/jobstream-frontend/package.json ./
COPY codebase/jobstream-frontend/package-lock.json ./
RUN npm ci

# Copie du code et build de production
COPY codebase/jobstream-frontend/ .
RUN npm run build -- --configuration=production

# Étape 2 : Serveur de production Nginx
FROM nginx:stable-alpine

COPY codebase/jobstream-frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/jobstream-frontend/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
