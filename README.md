# AnunciosLoc

# Desenvolvedores: Felícia Kianda, Wissel Filipe e Henrique Mendes
## Descrição

AnunciosLoc é um aplicativo Android desenvolvido em JAVA que permite aos usuários criar e visualizar anúncios baseados em localização geográfica. O app integra funcionalidades como mapas do Google, notificações push via Firebase, compartilhamento via WiFi Direct e sincronização offline, proporcionando uma experiência rica para anúncios locais.

O aplicativo foi desenvolvido para facilitar a criação de anúncios personalizados associados a locais específicos, com suporte a perfis de usuário, chaves de perfil para categorização e compartilhamento peer-to-peer.

## Funcionalidades Principais

- **Autenticação de Usuário**: Login e registro de contas.
- **Gerenciamento de Anúncios**: Criar, visualizar e salvar anúncios associados a locais.
- **Mapas Integrados**: Visualização de anúncios no mapa usando Google Maps.
- **Localização em Tempo Real**: Serviços de localização em foreground e background.
- **Notificações Push**: Recebimento de notificações via Firebase Messaging.
- **Compartilhamento via WiFi Direct**: Compartilhamento de anúncios entre dispositivos próximos.
- **Sincronização Offline**: Gerenciamento de dados quando offline.
- **Perfis de Usuário**: Edição de perfil, alteração de senha e gerenciamento de chaves de perfil.
- **Chaves de Perfil**: Sistema de chaves para categorizar e filtrar anúncios.

## Tecnologias Utilizadas

- **Linguagem**: JAVA
- **Framework**: Android SDK (minSdk 26, targetSdk 36)
- **Build Tool**: Gradle (JAVA DSL)
- **Arquitetura**: Activities, Services, Broadcast Receivers
- **Bibliotecas**:
  - Google Maps/Location Services
  - Firebase Messaging
  - Retrofit para API REST
  - Glide para carregamento de imagens
  - RecyclerView para listas
  - Material Design Components
  - Gson para serialização JSON
  - WiFi Direct para P2P

## Pré-requisitos

- **Android Studio**: Versão 2022.3.1 ou superior (para suporte ao AGP 8.13.0)
- **JDK**: Java 11
- **Dispositivo/Emulador**: Android API 26 ou superior
- **Google Play Services**: Instalado no dispositivo
- **Servidor Backend**: Um servidor Spring Boot ou similar rodando na URL configurada (padrão: http://192.168.39.157:8081)

## Instalação

1. **Clone o repositório**:
   ```bash
   git clone <url-do-repositorio>
   cd AnunciosLoc
   ```

2. **Abra no Android Studio**:
   - Abra o Android Studio
   - Selecione "Open" e navegue até a pasta do projeto
   - Aguarde a sincronização do Gradle

3. **Configure as APIs do Google**:
   - Obtenha uma chave da API do Google Maps em [Google Cloud Console](https://console.cloud.google.com/)
   - Substitua a chave no `AndroidManifest.xml`:
     ```xml
     <meta-data
         android:name="com.google.android.geo.API_KEY"
         android:value="SUA_CHAVE_AQUI" />
     ```
   - Habilite as seguintes APIs no Google Cloud:
     - Maps SDK for Android
     - Geocoding API
     - Places API (se necessário)

4. **Configure o Firebase**:
   - Crie um projeto no [Firebase Console](https://console.firebase.google.com/)
   - Baixe o `google-services.json` e substitua o arquivo existente em `app/google-services.json`
   - Habilite Firebase Cloud Messaging

5. **Configure o Servidor Backend**:
   - Certifique-se de que o servidor backend está rodando na URL especificada em `RetrofitClient.java`
   - Se necessário, altere a `BASE_URL` para o IP/endereço correto do seu servidor

## Como Rodar

1. **Build do Projeto**:
   - No Android Studio, clique em "Build" > "Make Project" ou use o atalho Ctrl+F9

2. **Executar no Dispositivo/Emulador**:
   - Conecte um dispositivo Android ou inicie um emulador
   - Clique em "Run" > "Run 'app'" ou use o atalho Shift+F10

3. **Depuração**:
   - Para debug, use breakpoints no código
   - Verifique os logs no Logcat do Android Studio

## Estrutura do Projeto

```
AnunciosLoc/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # Manifest do app com permissões e componentes
│   │   ├── java/ao/co/isptec/aplm/projetoanuncioloc/
│   │   │   ├── Adapters/                 # Adapters para RecyclerView
│   │   │   ├── Interface/                # Interfaces de callback
│   │   │   ├── Model/                    # Modelos de dados (Anuncio, User, etc.)
│   │   │   ├── Request/                  # Classes de request para API
│   │   │   ├── Service/                  # Serviços (API, localização, Firebase, etc.)
│   │   │   ├── wifidirect/               # Gerenciamento WiFi Direct
│   │   │   └── *.java                    # Activities e classes utilitárias
│   │   └── res/                          # Recursos (layouts, strings, drawables)
│   ├── build.gradle.kts                  # Configuração do módulo app
│   └── google-services.json              # Configuração Firebase
├── gradle/
│   └── libs.versions.toml                # Version catalog das dependências
├── build.gradle.kts                      # Configuração root
├── settings.gradle.kts                   # Configurações do projeto
└── README.md
```

## API Backend

O aplicativo se comunica com um backend via REST API usando Retrofit. As principais endpoints incluem:

- **Autenticação**: `/login`, `/register`
- **Anúncios**: `/anuncios` (GET, POST, PUT, DELETE)
- **Locais**: `/locais`
- **Usuários**: `/users`
- **Notificações**: `/notificacoes`

Certifique-se de que o servidor backend implementa essas rotas conforme definido em `ApiService.java`.

## Permissões Necessárias

O app requer as seguintes permissões (declaradas no AndroidManifest.xml):

- `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`: Para localização
- `INTERNET`, `ACCESS_NETWORK_STATE`: Para comunicação com API
- `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`: Para serviços em foreground
- `POST_NOTIFICATIONS`: Para notificações push
- `READ_EXTERNAL_STORAGE`, `READ_MEDIA_IMAGES`: Para acesso a imagens
- `BLUETOOTH`, `BLUETOOTH_ADMIN`: Para WiFi Direct
- `RECEIVE_BOOT_COMPLETED`: Para reiniciar serviços após boot

## Configurações Adicionais

### ProGuard

Para builds de release, o ProGuard está configurado em `proguard-rules.pro`. Atualmente, minify está desabilitado (`isMinifyEnabled = false`), mas pode ser habilitado para otimização.

### Build Variants

- **Debug**: Para desenvolvimento
- **Release**: Para produção (com ProGuard opcional)

## Testes

O projeto inclui configuração básica para testes:

- **Unit Tests**: Usando JUnit 4
- **Instrumentation Tests**: Usando Espresso

Para executar testes:
- Unit tests: `./gradlew test`
- Instrumentation tests: `./gradlew connectedAndroidTest`

## Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -am 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

### Diretrizes de Código

- Use JAVA como linguagem principal
- Siga as convenções de nomenclatura do Android
- Mantenha Activities pequenas e use Fragments se necessário
- Documente métodos complexos
- Teste suas mudanças

## Problemas Conhecidos

- A chave do Google Maps está hardcoded no código (substitua pela sua)
- O IP do servidor backend pode precisar ser ajustado dependendo da rede
- WiFi Direct requer dispositivos com suporte ao hardware

## Licença

Este projeto está sob a licença [MIT](LICENSE). Veja o arquivo LICENSE para mais detalhes.

## Suporte

Para suporte ou dúvidas:
- Abra uma issue no repositório
- Entre em contato com a equipe de desenvolvimento

---

**Nota**: Este README foi gerado baseado na análise do código fonte. Algumas configurações podem precisar de ajustes específicos do ambiente de desenvolvimento.</content>
<parameter name="filePath">c:\Users\hp\AndroidStudioProjects\backup1\AnunciosLoc\README.md