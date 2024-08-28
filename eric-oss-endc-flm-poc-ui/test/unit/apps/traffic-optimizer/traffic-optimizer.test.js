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
/**
 * Integration tests for <e-traffic-optimizer>
 */
import { expect, fixture } from '@open-wc/testing';
import '../../../../src/apps/traffic-optimizer/traffic-optimizer.js';

describe('TrafficOptimizer Application Tests', () => {
  describe('Basic application setup', () => {
    it('should create a new <e-traffic-optimizer>', async () => {
      const element = await fixture(
        '<e-traffic-optimizer></e-traffic-optimizer>',
      );
      const headingTag = element.shadowRoot.querySelector('h1');

      expect(
        headingTag.textContent,
        '"Your app markup goes here" was not found',
      ).to.equal('Your app markup goes here');
    });
  });
});
