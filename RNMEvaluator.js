/**
 *
 * @providesModule RNMEvaluator
 * @flow
 */
'use strict';

var {
  DeviceEventEmitter,
  NativeModules: {
    RNMEvaluator
  }
} = require('react-native');

var execute = (data, executor) => {
  var {name,args,callId} = data,
      cb = res => {
        if (res instanceof Promise) {
          res.then(result => RNMEvaluator.functionCallCompleted(callId, null, {_value: result}));
          return;
        }
      	if (res instanceof Error) {
      	  RNMEvaluator.functionCallCompleted(callId, `Function ${name} raised an error ${res.message}:${res.stack}`,null);
      	} else {
      	   RNMEvaluator.functionCallCompleted(callId, null, { _value:res });           
        }
      }, func,err;

  try {
    func = eval(name);
  } catch (e) {
    err = e;
  }

  if (err) {
    return RNMEvaluator.functionCallCompleted(callId, `Error finding a function ${err.message}`,null);
  }

  if (!func) {
    return RNMEvaluator.functionCallCompleted(callId, `Function ${name} cannot be found`,null);
  }

  if (typeof func !== 'function') {
    return RNMEvaluator.functionCallCompleted(callId, `${name} is not a function`,null);
  }

  try {
    executor(func).apply(null,args.concat(cb))
  } catch (e) {
    RNMEvaluator.functionCallCompleted(callId, `Function ${name} raised an error ${e.message}:${e.stack}`,null);
  }
}

// TODO: move to AppEventEmitter once App events are supported on android.
DeviceEventEmitter.addListener('RNMEvaluator.callAsyncFunction',
				  data => execute(data, f => f));

DeviceEventEmitter.addListener('RNMEvaluator.callSyncFunction',
				  data => execute(data, f => (...args) => args.pop()(f.apply(null,args))));

exports.emit = (name, res) => {
  RNMEvaluator.emit(name, { _value:res });
};
