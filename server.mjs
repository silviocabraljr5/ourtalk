import http from 'node:http';
import { readFile, stat } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { randomUUID } from 'node:crypto';

const root=path.join(path.dirname(fileURLToPath(import.meta.url)),'dist');
const handle=process.env.INFINITEPAY_HANDLE||'ourworld-ltda';
const port=Number(process.env.PORT||8080);
const allowedOrigin=process.env.SITE_ORIGIN||'';
const limits=new Map();
const json=(res,status,data)=>{res.writeHead(status,{'Content-Type':'application/json; charset=utf-8','Cache-Control':'no-store','X-Content-Type-Options':'nosniff'});res.end(JSON.stringify(data));};
export function validateAmount(value){return Number.isSafeInteger(value)&&value>=100&&value<=100000000;}
export async function createCheckout(amountCents,fetcher=fetch){
 if(!validateAmount(amountCents))throw new Error('invalid_amount');
 const response=await fetcher('https://api.checkout.infinitepay.io/links',{method:'POST',headers:{'Content-Type':'application/json'},signal:AbortSignal.timeout(15000),body:JSON.stringify({handle,items:[{description:'Contribuição voluntária ao desenvolvimento do OurTalk — OurWorld',price:amountCents,quantity:1}],order_nsu:'ourtalk-'+randomUUID()})});
 if(!response.ok)throw new Error('provider_unavailable');
 const data=await response.json();const url=new URL(data.url);
 if(url.protocol!=='https:'||!['buy.infinitepay.io','link.infinitepay.io','checkout.infinitepay.io'].includes(url.hostname))throw new Error('invalid_provider_url');
 return {url:url.href};
}
const mime={'.mjs':'text/javascript; charset=utf-8','.html':'text/html; charset=utf-8','.css':'text/css; charset=utf-8','.js':'text/javascript; charset=utf-8','.json':'application/json; charset=utf-8','.png':'image/png','.jpg':'image/jpeg','.svg':'image/svg+xml','.txt':'text/plain; charset=utf-8'};
export const server=http.createServer(async(req,res)=>{
 try{
  const url=new URL(req.url,'http://localhost');
  if(url.pathname==='/newsletter.php')return json(res,503,{error:'development_server'});
  if(url.pathname==='/payment-config.json'&&req.method==='GET')return json(res,200,{mode:'api',merchant:'OurWorld',receiverCnpj:'32.928.642/0001-41'});
  if(url.pathname==='/api/checkout'){
   if(req.method!=='POST')return json(res,405,{error:'method_not_allowed'});
   const origin=req.headers.origin;const expected=allowedOrigin||'http://'+req.headers.host;
   if(!origin||origin!==expected)return json(res,403,{error:'invalid_origin'});
   if(!(req.headers['content-type']||'').startsWith('application/json'))return json(res,415,{error:'json_required'});
   const ip=req.socket.remoteAddress||'local';const now=Date.now();const previous=limits.get(ip);const bucket=previous&&previous.until>now?previous:{count:0,until:now+60000};bucket.count++;limits.set(ip,bucket);
   if(limits.size>10000)for(const[key,value]of limits)if(value.until<now)limits.delete(key);
   if(bucket.count>8)return json(res,429,{error:'try_later'});
   let body='';for await(const chunk of req){body+=chunk;if(body.length>2048)return json(res,413,{error:'too_large'});}
   let data;try{data=JSON.parse(body);}catch{return json(res,400,{error:'invalid_json'});}
   if(!validateAmount(data.amountCents))return json(res,400,{error:'invalid_amount'});
   try{return json(res,200,await createCheckout(data.amountCents));}catch{return json(res,502,{error:'checkout_unavailable'});}
  }
  if(!['GET','HEAD'].includes(req.method))return json(res,405,{error:'method_not_allowed'});
  let name;try{name=decodeURIComponent(url.pathname);}catch{return json(res,400,{error:'invalid_path'});}
  const file=path.resolve(root,'.'+(name==='/'?'/index.html':name));
  if(!file.startsWith(root+path.sep))return json(res,403,{error:'invalid_path'});
  if(!(await stat(file)).isFile())return json(res,404,{error:'not_found'});
  res.writeHead(200,{'Content-Type':mime[path.extname(file)]||'application/octet-stream','X-Content-Type-Options':'nosniff','Referrer-Policy':'strict-origin-when-cross-origin','Cache-Control':'no-cache'});
  if(req.method==='HEAD')return res.end();res.end(await readFile(file));
 }catch{return json(res,404,{error:'not_found'});}
});
if(process.argv[1]&&path.resolve(process.argv[1])===fileURLToPath(import.meta.url)){server.listen(port,'127.0.0.1',()=>console.log('OurTalk: http://localhost:'+port));}
