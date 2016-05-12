'use strict';

var feedbackUrl = 'http://hootch.test.netflix.net/submit';
var gateHost = '{gate}';
var bakeryDetailUrl = 'http://bakery.test.netflix.net/#/?region={{context.region}}&package={{context.package}}&detail=bake:{{context.status.resourceId}}';
var authEndpoint = 'https://spinnaker-api-prestaging.prod.netflix.net/auth/info';

window.spinnakerSettings = {
  defaultProviders: ['aws', 'cf'],
  feedbackUrl: feedbackUrl,
  gateUrl: gateHost,
  bakeryDetailUrl: bakeryDetailUrl,
  authEndpoint: authEndpoint,
  pollSchedule: 30000,
  defaultTimeZone: 'America/Los_Angeles', // see http://momentjs.com/timezone/docs/#/data-utilities/
  providers: {
    cf: {
      defaults: {
        account: 'prod',
        region: 'spinnaker'
      },
      primaryAccounts: ['prod'],
      primaryRegions: ['spinnaker'],
      challengeDestructiveActions: ['prod'],
      defaultSecurityGroups: [],
      accountBastions : {
      },
      preferredZonesByAccount: {
        prod: {
          'spinnaker': ['production']
        },
        default: {
          'spinnaker': ['production']
        }
      }
    }
  },
  whatsNew: {
    gistId: '32526cd608db3d811b38',
    fileName: 'news.md',
  },
  authEnabled: 'disabled',
  feature: {
    pipelines: true,
    notifications: false,
    fastProperty: true,
    vpcMigrator: true,
    clusterDiff: true,
    rebakeControlEnabled: false,
    netflixMode: false,
  },
};
