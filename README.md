# Emrex-gateway

Gateway between Emrex and DC4EU wallet. This will let you export transcript of records from your Emrex-connected university into a digital wallet. 

We are also planning on exporting transcript of records from a wallet to a Emrex-connected university

## Other projects this project depends on

[vc](https://github.com/dc4eu/vc/)

[converter](https://gitlab.govpart.de/dc4eu-converter/dc4eu-converter/)

## We are planning to support the wwWallet (see links below)

[wwWallet](https://demo.wwwallet.org/)

[dc4eu Wallet](https://wallet.dc4eu.eu/)

## Execution

You can start the application from maven:

```
mvn clean install
mvn spring-boot:run
```

or using docker-compose:

You can start the application by running the following command: just set reguired environment variables in the docker-compose.yml if the default values are not suitable for you.
```
docker-compose up --build
```

Then you can access the application on http://localhost:8080


