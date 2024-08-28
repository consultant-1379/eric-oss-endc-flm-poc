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
import { rmSync } from 'fs';
import { generateRollup, outDirectory } from '@eui/rollup-config-generator';

// just make sure our output dir is clean
try {
  rmSync(outDirectory, { recursive: true });
} catch (e) {
  // None exists
}

const userRollupConfig = {
  // Anything not covered by config.package.json
  externals: [],
  importMap: {},
};

const { euisdkRollupConfig } = generateRollup(userRollupConfig);

export default [euisdkRollupConfig];
