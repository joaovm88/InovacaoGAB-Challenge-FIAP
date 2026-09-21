# InovaGAB App (Android)

App do Challenge FIAP — Grupo Águia Branca. A partir da Sprint 2, o app não
usa mais Firebase Auth/Realtime Database como fonte de dados: todas as
telas consomem a API REST real do backend Spring Boot em
`inovagab/inovagab`.

## Como apontar o app para o backend

O endereço do servidor fica em `ApiClient.baseUrl`
(`app/src/main/java/br/com/fiap/inovacaogab/data/ApiClient.kt`):

```kotlin
var baseUrl: String = "http://10.0.2.2:8080/"
```

- **Emulador Android**: não precisa mudar nada. `10.0.2.2` é o alias que o
  próprio emulador usa para acessar o `localhost` da máquina onde ele roda
  — ou seja, se o backend estiver rodando em `localhost:8080` na sua
  máquina, o emulador já enxerga.
- **Aparelho físico (celular de verdade)**: troque para o IP da máquina
  que está rodando o backend na mesma rede Wi-Fi, por exemplo
  `"http://192.168.0.10:8080/"`, e adicione esse mesmo IP em
  `res/xml/network_security_config.xml` (a lista de hosts liberados para
  tráfego HTTP sem TLS, já que este é um backend de desenvolvimento sem
  certificado).

Antes de testar, garanta que o backend está rodando (`./mvnw spring-boot:run`
dentro de `inovagab/inovagab`) e acessível a partir do emulador/dispositivo.

## O que mudou em relação à Sprint 1

- Login/Cadastro passaram a chamar `/api/auth/login` e `/api/auth/registro`,
  guardando o token JWT retornado (`SessionManager`, via SharedPreferences)
  em vez de usar `FirebaseAuth`.
- Mural, Ideias, Projetos e Dashboard passaram a consumir os endpoints reais
  do backend (`/api/estrategias`, `/api/ideias`, `/api/projetos`,
  `/api/dashboard/resultados`) em vez do Firebase Realtime Database.
- Cada tela respeita as mesmas regras de perfil aplicadas no backend
  (ex.: só Líder cria/edita estratégias; só Gestor cria/edita projetos; o
  Dashboard é exclusivo do Líder).

## Limitação conhecida

Este ambiente de desenvolvimento (onde as mudanças foram implementadas) não
possui o Android SDK/emulador instalado, então as telas não puderam ser
compiladas/testadas ponta a ponta aqui. Antes da entrega final, abra o
projeto no Android Studio, sincronize o Gradle e rode o app em um emulador
apontando para o backend local para validar o fluxo completo (login,
cadastro/avaliação de ideias, mural, projetos e dashboard).
