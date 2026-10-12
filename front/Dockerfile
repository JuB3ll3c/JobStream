FROM node:24-alpine AS build
RUN apk add --no-cache openjdk25-jre
WORKDIR /app
COPY front/package*.json ./front/
WORKDIR /app/front
RUN npm ci

WORKDIR /app
COPY front ./front
COPY openapi ./openapi

WORKDIR /app/front
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/front/dist/front/browser /usr/share/nginx/html
COPY front/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
