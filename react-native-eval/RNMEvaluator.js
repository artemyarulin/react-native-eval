/**
 *
 * @providesModule RNMEvaluator
 * @flow
 */
'use strict';

var { NativeAppEventEmitter,
      NativeModules: {
	RNMEvaluator
      }
    } = require('react-native')

var execute = (data, executor) => {
  var {name,args,callId} = data,
      cb = res => {
	if (res instanceof Error)
	  RNMEvaluator.functionCallCompleted(callId,`Function ${name} raised an error ${res.message}:${res.stack}`,null)
	else
	  RNMEvaluator.functionCallCompleted(callId,null,res)
      },
      func,err,res

  try { func = eval(name) }
  catch (e) { err = e }

  if (err)
    return RNMEvaluator.functionCallCompleted(callId,`Error finding a function ${err.message}`,null)

  if (!func)
    return RNMEvaluator.functionCallCompleted(callId,`Function ${name} cannot be found`,null)

  if (typeof func !== 'function')
    return RNMEvaluator.functionCallCompleted(callId,`${name} is not a function`,null)

  try { console.log('before'); res = executor(func).apply(null,args.concat(cb)); console.log('after') }
  catch (e) { RNMEvaluator.functionCallCompleted(callId,`Function ${name} raised an error ${e.message}:${e.stack}`,null) }
}

NativeAppEventEmitter.addListener('RNMEvaluator.callAsyncFunction',
				  data => execute(data, f => f))

NativeAppEventEmitter.addListener('RNMEvaluator.callSyncFunction',
				  data => execute(data, f => (...args) => args.pop()(f.apply(null,args))))
