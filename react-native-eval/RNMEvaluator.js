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

NativeAppEventEmitter.addListener('RNMEvaluator.callFunctionSync',data => {
    var {name,args,callId} = data,
	func,err,res
    
    try { func = eval(name) }
    catch (e) { err = e }		
    
    if (err)		
	return RNMEvaluator.functionCallCompleted(callId,`Error finding a function ${err.message}`,null)
    
    if (!func)		
	return RNMEvaluator.functionCallCompleted(callId,`Function ${name} cannot be found`,null)
    
    if (typeof func === 'function')	{
	try { res = func.apply(null,args) }
	catch (e) { err = e }
	if (err)
	    return RNMEvaluator.functionCallCompleted(callId,`Function ${name} raised an error ${err.message}:${err.stack}`,null)
	else
	    return RNMEvaluator.functionCallCompleted(callId,null,res)
    }
    else
	return RNMEvaluator.functionCallCompleted(callId,`${name} is not a function`,null)
})
