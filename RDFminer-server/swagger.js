const swaggerAutogen = require('swagger-autogen')();

const doc = {
  info: {
    version: '1.0.0',      // by default: '1.0.0'
    title: 'RDFMiner REST API',        // by default: 'REST API'
    description: 'This is the official documentation of RDFMiner web services',  // by default: ''
  },
//   host: '',      // by default: 'localhost:3000'
//   basePath: '',  // by default: '/'
//   schemes: [],   // by default: ['http']
//   consumes: [],  // by default: ['application/json']
//   produces: [],  // by default: ['application/json']
//   tags: [        // by default: empty Array
//     {
//       name: '',         // Tag name
//       description: '',  // Tag description
//     },
    // { ... }
//   ],
//   securityDefinitions: {},  // by default: empty object
//   definitions: {},          // by default: empty object (Swagger 2.0)
//   components: {}            // by default: empty object (OpenAPI 3.x)
};

const outputFile = './doc/swagger.json';
const endpointsFiles = ['./app.js'];

/* NOTE: if you use the express Router, you must pass in the 
   'endpointsFiles' only the root file where the route starts,
   such as: index.js, app.js, routes.js, ... */

swaggerAutogen(outputFile, endpointsFiles, doc);