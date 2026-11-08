export function crc16(text){let crc=0xffff;for(const byte of new TextEncoder().encode(text)){crc^=byte<<8;for(let bit=0;bit<8;bit++)crc=((crc<<1)^((crc&0x8000)?0x1021:0))&0xffff;}return crc.toString(16).toUpperCase().padStart(4,'0');}
const field=(id,value)=>id+String(new TextEncoder().encode(value).length).padStart(2,'0')+value;
export function createPixCode(cents){if(!Number.isSafeInteger(cents)||cents<100||cents>100000000)throw new RangeError('invalid_amount');const payload=field('00','01')+field('26',field('00','br.gov.bcb.pix')+field('01','32928642000141'))+field('52','0000')+field('53','986')+field('54',(cents/100).toFixed(2))+field('58','BR')+field('59','OURWORLD LTDA')+field('60','BRASIL')+field('62',field('05','***'))+'6304';return payload+crc16(payload);}
if(typeof document!=='undefined'){
 const input=document.getElementById('donation-amount'),panel=document.getElementById('pix-generated'),code=document.getElementById('pix-code'),feedback=document.getElementById('pix-feedback');
 document.getElementById('generate-pix')?.addEventListener('click',()=>{if(!input.reportValidity())return;try{code.value=createPixCode(Math.round(Number(input.value)*100));panel.hidden=false;feedback.textContent='';}catch{feedback.textContent='Confira o valor da sua contribuição.';}});
 input?.addEventListener('input',()=>{panel.hidden=true;code.value='';feedback.textContent='';});
 document.getElementById('copy-generated-pix')?.addEventListener('click',async()=>{try{await navigator.clipboard.writeText(code.value);feedback.textContent='Código Pix copiado!';}catch{code.focus();code.select();feedback.textContent='Selecione e copie o código acima.';}});
}
