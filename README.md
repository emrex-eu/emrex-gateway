# Emrex-gateway

Gateway between Emrex and DC4EU wallet. This will let you export transcript of records from your Emrex-connected university into a digital wallet. 

We are also planning on exporting transcript of records from a wallet to a Emrex-connected university

## Other projects this project depends on

[vc](https://github.com/dc4eu/vc/)

[converter](https://gitlab.govpart.de/dc4eu-converter/dc4eu-converter/)

## Wallets we are planning to support

[wwWallet](https://demo.wwwallet.org/)

## Execution

You can start the application from maven:

```
mvn clean install
mvn spring-boot:run
```

or using docker-compose:

```
docker-compose up --build
```



