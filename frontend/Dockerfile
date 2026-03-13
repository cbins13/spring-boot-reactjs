# Build & serve the React (Vite) frontend
FROM node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Inject API base URL at build time. In Docker+nginx, we want same-origin requests (/api/*),
# so default to empty string.
ARG VITE_API_BASE_URL=
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

RUN npm run build

FROM node:20-alpine AS runtime
WORKDIR /app

COPY --from=build /app/package.json /app/package-lock.json ./
# Vite preview requires the Vite CLI at runtime; keep dev deps in this image.
RUN npm ci --include=dev

ENV NODE_ENV=production

COPY --from=build /app/dist ./dist

EXPOSE 4173
CMD ["npm","run","preview","--","--host","0.0.0.0","--port","4173"]

