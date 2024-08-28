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
/* eslint consistent-return:0 */
import { string } from 'rollup-plugin-string';
import { fromRollup } from '@web/dev-server-rollup';

const replaceCss = fromRollup(string);

const hmr = process.argv.includes('--hmr');

export default /** @type {import('@web/dev-server').DevServerConfig} */ ({
  nodeResolve: {
    extensions: ['.mjs', '.cjs', '.js'],
  },
  open: './',
  watch: !hmr,
  plugins: [replaceCss({ include: ['**/*.css', '**/*.json'] })],
  mimeTypes: {
    // es-module-shim will convert to cssStylesheet, import for definition needs to be a string
    // Force application/javascript mimetype for all other css files
    'src/**/*.css': 'js',
  },
  middleware: [
    // Middleware uses Koa syntax -> https://github.com/koajs/koa
    // Warning!!!: Don't use destructuring on context when reading values, throws errors
    function mockExternals(context, next) {
      // Sample for mocking external service responses
      // if(context.url === '/rest/v1'){
      //   context.body = {
      //     mock: 'response',
      //   }
      //   // Return undefined serves the result in current state
      //   return;
      // }

      // Signal move to next middleware function, do not remove
      return next();
    },
    function publicAssets(context, next) {
      // Don't mess with any of these requests
      const nonPublic = ['/src', '/node_modules', '/__web-dev-server'];
      for (const folder of nonPublic) {
        if (context.url.startsWith(folder)) {
          return next();
        }
      }

      // Send everything else to public folder
      context.url = `public${context.url}`;
      return next();
    },
    // Do not define any further middleware after this point
    // Anything marked with next will now be passed through
    // the web-dev-server middleware and compiled if needed.
  ],
});
