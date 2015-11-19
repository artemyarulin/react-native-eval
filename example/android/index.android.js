var rneval = require('react-native-eval');

// puts the object in the global namespace
global.AProject = {
  load: function() {
    rneval.emit('loaded', { message: 'javascript loaded' });
  }
};
