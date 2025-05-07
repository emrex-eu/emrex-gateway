# Emrex-gateway

Gateway between Emrex and a wallet. This will let you export credentials from your Emrex-connected university into a digital wallet and vice versa using the ELM format. 

## Other projects this project depends on

[vc](https://github.com/dc4eu/vc/)

[converter](https://gitlab.govpart.de/dc4eu-converter/dc4eu-converter/)

## We are planning to support at least the wallets below

[wwWallet](https://demo.wwwallet.org/)

[dc4eu Wallet](https://wallet.dc4eu.eu/)

## Execution

You can start the application from maven:

```
mvn clean install
mvn spring-boot:run
```

or using docker-compose (recommended):

You can start the application by running the following command: just set reguired environment variables in the docker-compose.yml if the default values are not suitable for you.
```
docker compose up --build
```

Then you can access the application on http://localhost:8080

## Using the application

The application consists of three main parts:
- "Add to wallet" - lets you add credentials to your wallet
- "Export from wallet" - lets you export credentials from your wallet
- "Show incoming credentials" - shows the latest incoming credentials exported from wallets

### Add to wallet

Select the country and the university you want to add the credential from. 
The application will then generate a QR code that you can scan with your wallet app. 
The QR code contains a link to the credential in the Emrex system.

For debug purposes you can also upload an elmo file directly (the signature will not be validated in this case) 

### Export from wallet

A QR code will be generated that you can scan with your wallet app. 
Select the ELM data from the wallet that you want to export

### Show incoming credentials

The latest incoming credentials will be displayed in a table (most recent on top). 
You can also download the elmo file for each credential.

## Configuration

- You can run the issuer (vc) locally or use a hosted version. Use the property dc4eu.issuer.url to set the URL of the issuer. (Might be overrided by the DC4EU_ISSUER_URL environment variable)
- The default is to use the test emreg registry. You can set the property emrex.registry.url to use a different registry. (Might be overrided by the EMREX_REGISTRY_URL environment variable)
- The docker image for the converter (that converts between the ELMO and ELM formats) is by default picked from: dockerrepo.govpart.de/dc4euconverter:latest Property: dc4eu.converter.url
- The directory imported_elmos is used to store the elmo files that are imported from wallets. Property: imported.elmo.directory
