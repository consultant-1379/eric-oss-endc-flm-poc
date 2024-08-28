/*
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 */
/* eslint import/no-extraneous-dependencies:0 */
import { fileURLToPath } from 'url';
import path from 'path';
import express from 'express';

const app = express();
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const port = process.env.port || 3010;

app.use(express.static('build'));

app.get('/', (req, res) => {
  res.sendFile(`${__dirname}/build/index.html`);
});

app.listen(port, () => {
  /* eslint-disable-next-line */
  console.log(
    `MF Service - "E-UI SDK Skeleton" is running on port http://localhost:${port}`,
  );
});
