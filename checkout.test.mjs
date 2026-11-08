import test from 'node:test';
import assert from 'node:assert/strict';
import {validateAmount,createCheckout} from './server.mjs';
test('accepts integer cents within contribution limits',()=>{assert.equal(validateAmount(100),true);assert.equal(validateAmount(100000000),true);for(const amount of [0,99,-1,1.5,NaN,Infinity,100000001,'5000'])assert.equal(validateAmount(amount),false);});
test('creates a checkout for the confirmed merchant without sending donor data',async()=>{let payload;const result=await createCheckout(5000,async(url,options)=>{assert.equal(url,'https://api.checkout.infinitepay.io/links');payload=JSON.parse(options.body);return {ok:true,json:async()=>({url:'https://buy.infinitepay.io/example'})};});assert.equal(payload.handle,'ourworld-ltda');assert.equal(payload.items[0].price,5000);assert.equal(payload.items[0].quantity,1);assert.equal(payload.customer,undefined);assert.equal(result.url,'https://buy.infinitepay.io/example');});
test('rejects an unexpected checkout destination',async()=>{await assert.rejects(createCheckout(5000,async()=>({ok:true,json:async()=>({url:'https://example.com/pay'})})),/invalid_provider_url/);});
