
# Overview

This is a skeleton implementation of E-UI SDK Project. It can be used as a starting point to build up UIs for both Standalone and Micro Frontend projects using the E-UI SDK framework.

The project consists of the E-UI SDK Container & Theme libraries and a basic server file which used to host the project build locally.

---

# Public

The public folder contains 4 files which are listed below.

| name                | description                                                     |
|---------------------|-----------------------------------------------------------------|
| config.json         | configure the navigation menu based on ADP UI-Meta schema       |
| config.package.json | configure modules made available to the GUI Aggregator Service. |
| index.css           | style the Container                                             |
| index.html          | import and run the Theme and Container                          |

---

# Installation

`$ npm install`

---

# Testing

[`@open-wc/testing`](https://open-wc.org/docs/testing/testing-package/) is used to unit test all components. It is an opinionated package that combines and configures testing libraries to minimize the amount of ceremony required when writing tests.  

## Running tests

Run all tests against the Firefox headless browser.  

``` shell
npm run test
```

Run all tests against all headless browsers (chrome, firefox and Safari).

``` shell
npm run test:all
```

Run all tests against Chrome headless browser in watch mode.

``` shell
npm run test:watch
```

---

# Development

To run the application in development mode, execute the following command. A browser will open and display the apps included in the `config.json` file.  

```shell
npm start
```

---

# Serve the Apps

It is possible to serve a built version of the applications. You must first build the project using `dev` or `prod` mode.

**Build the E-UI SDK Project**

```shell
npm run build:dev
```

```shell
npm run build:prod
```

**Serve the E-UI SDK Project**

```shell
npm run srv
```
