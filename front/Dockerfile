FROM nginx:alpine

COPY front/nginx.conf /etc/nginx/conf.d/default.conf
COPY .ci-artifacts/frontend/ /usr/share/nginx/html/

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
