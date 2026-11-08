const menu = document.querySelector('.menu');
const nav = document.querySelector('nav');
menu.addEventListener('click', () => { const open = menu.getAttribute('aria-expanded') !== 'true'; menu.setAttribute('aria-expanded', String(open)); menu.setAttribute('aria-label', open ? 'Fechar menu' : 'Abrir menu'); nav.classList.toggle('open', open); });
nav.querySelectorAll('a').forEach(a => a.addEventListener('click', () => { nav.classList.remove('open'); menu.setAttribute('aria-expanded', 'false'); menu.setAttribute('aria-label', 'Abrir menu'); }));
document.getElementById('year').textContent = String(new Date().getFullYear());



document.querySelectorAll('.ux-app').forEach(app => { app.querySelectorAll('[data-go]').forEach(button => button.addEventListener('click', () => { const target = button.dataset.go; app.querySelectorAll('[data-view]').forEach(view => view.classList.toggle('active',view.dataset.view === target)); app.querySelectorAll('.ux-nav [data-go], .ux-top-resources').forEach(nav => { const section = ['adicionar','qr','convite','proximos','privacidade','conviteapp'].includes(target) ? 'conversas' : ['cidade','pontos'].includes(target) ? 'cidade' : target === 'assistente' ? 'assistente' : ['biblia','saude','cofre','preparar','tempo','comunidade'].includes(target) ? 'mais' : target; const active = nav.dataset.go === section; nav.classList.toggle('active',active); nav.setAttribute('aria-pressed',String(active)); }); app.querySelector('.ux-content').scrollTop=0; })); });
document.querySelectorAll('.ux-app').forEach(app => { app.querySelectorAll('[data-context]').forEach(button => button.addEventListener('click', () => { app.querySelectorAll('[data-context]').forEach(tab => { const active=tab===button;tab.classList.toggle('active',active);tab.setAttribute('aria-pressed',String(active)); }); app.querySelectorAll('[data-pane]').forEach(pane=>pane.classList.toggle('active',pane.dataset.pane===button.dataset.context)); })); });


document.querySelectorAll('.ux-app').forEach(app=>{
 const search=app.querySelector('.resource-search input'); let filter='Todos';
 const normalize=text=>text.normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLowerCase();
 function filterResources(){let count=0;const q=normalize(search?.value||'');app.querySelectorAll('.resource-group').forEach(group=>{let visible=0;group.querySelectorAll('.resource-item').forEach(item=>{const show=(filter==='Todos'||group.dataset.group===filter)&&normalize(item.dataset.search).includes(q);item.hidden=!show;item.style.display=show?'':'none';if(show){visible++;count++;}});group.hidden=!visible;});const empty=app.querySelector('.resource-no-results');if(empty)empty.hidden=count>0;}
 search?.addEventListener('input',filterResources);app.querySelectorAll('[data-filter]').forEach(button=>button.addEventListener('click',()=>{filter=button.dataset.filter;app.querySelectorAll('[data-filter]').forEach(tab=>{tab.classList.toggle('active',tab===button);tab.setAttribute('aria-pressed',String(tab===button));});filterResources();}));
 app.querySelectorAll('[data-people]').forEach(button=>button.addEventListener('click',()=>{app.querySelectorAll('[data-people]').forEach(tab=>{tab.classList.toggle('active',tab===button);tab.setAttribute('aria-pressed',String(tab===button));});app.querySelectorAll('[data-people-pane]').forEach(pane=>pane.classList.toggle('active',pane.dataset.peoplePane===button.dataset.people));}));
 app.querySelectorAll('[data-share-project]').forEach(button=>button.addEventListener('click',async()=>{const feedback=app.querySelector('.invite-feedback');const url='https://ourtalk.com.br/';try{if(navigator.share){await navigator.share({title:'Conheça o OurTalk',text:'Um projeto de comunicação, informação e apoio entre pessoas. Em desenvolvimento.',url});feedback.textContent='Convite compartilhado.';}else{await navigator.clipboard.writeText(url);feedback.textContent='Link copiado: '+url;}}catch(error){if(error.name!=='AbortError')feedback.textContent='Compartilhe este endereço: '+url;}}));
});

document.querySelectorAll('[data-scenario]').forEach(button=>button.addEventListener('click',()=>{document.querySelectorAll('[data-scenario]').forEach(tab=>{tab.classList.toggle('active',tab===button);tab.setAttribute('aria-pressed',String(tab===button));});document.querySelectorAll('[data-scenario-pane]').forEach(pane=>pane.classList.toggle('active',pane.dataset.scenarioPane===button.dataset.scenario));}));
const donationAmount=document.getElementById('donation-amount');
document.querySelectorAll('[data-donation]').forEach(button=>button.addEventListener('click',()=>{donationAmount.value=button.dataset.donation;document.querySelectorAll('[data-donation]').forEach(preset=>{preset.classList.toggle('active',preset===button);preset.setAttribute('aria-pressed',String(preset===button));});}));
donationAmount?.addEventListener('input',()=>document.querySelectorAll('[data-donation]').forEach(button=>{const active=Number(button.dataset.donation)===Number(donationAmount.value);button.classList.toggle('active',active);button.setAttribute('aria-pressed',String(active));}));
// Public project journal. Content is maintained in project-status.json.
fetch('project-status.json',{cache:'no-store'}).then(r=>{if(!r.ok)throw Error('status unavailable');return r.json();}).then(data=>{
 const dateLabel=value=>new Intl.DateTimeFormat('pt-BR',{day:'2-digit',month:'long',year:'numeric',timeZone:'UTC'}).format(new Date(value+'T12:00:00Z'));
 const node=(tag,text,className)=>{const el=document.createElement(tag);if(text)el.textContent=text;if(className)el.className=className;return el;};
 const updated=document.getElementById('project-updated');if(updated&&/^\d{4}-\d{2}-\d{2}$/.test(data.updatedAt)){updated.dateTime=data.updatedAt;updated.textContent=dateLabel(data.updatedAt);}
 const stages=document.getElementById('project-stages');if(stages&&Array.isArray(data.stages)&&data.stages.length){const items=data.stages.map(stage=>{const article=node('article');article.append(node('span',stage.label,'stage-state '+(['complete','ongoing','next'].includes(stage.status)?stage.status:'next')),node('h3',stage.title),node('p',stage.description));return article;});stages.replaceChildren(...items);}
 const journal=document.getElementById('project-updates');if(journal&&Array.isArray(data.updates)&&data.updates.length){const items=data.updates.filter(update=>/^\d{4}-\d{2}-\d{2}$/.test(update.date)).sort((a,b)=>b.date.localeCompare(a.date)).map(update=>{const article=node('article'),time=node('time',dateLabel(update.date)),body=node('div');time.dateTime=update.date;body.append(node('h4',update.title),node('p',update.description));article.append(time,body);return article;});journal.replaceChildren(...items);}
}).catch(()=>{/* Keep the published HTML when the feed is unavailable. */});
const newsletter=document.getElementById('newsletter-form');
newsletter?.addEventListener('submit',async event=>{
 event.preventDefault();const feedback=document.getElementById('newsletter-feedback');const button=newsletter.querySelector('button[type="submit"]');
 if(!newsletter.reportValidity())return;
 const form=new FormData(newsletter);const email=String(form.get('email')||'').trim();const whatsapp=String(form.get('whatsapp')||'').trim();
 if(!email&&!whatsapp){feedback.textContent='Informe seu e-mail ou WhatsApp para receber o aviso.';return;}
 button.disabled=true;feedback.textContent='Salvando seu cadastro…';
 try{const response=await fetch('newsletter.php',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({email,whatsapp,company:String(form.get('company')||''),consent:form.get('consent')==='on'})});const result=await response.json();if(!response.ok){const messages={invalid_email:'Confira seu e-mail.',invalid_whatsapp:'Confira o WhatsApp com DDD.',consent_required:'Autorize o recebimento do aviso para continuar.',try_later:'Aguarde um pouco antes de tentar novamente.'};throw new Error(messages[result.error]||'Não foi possível salvar agora. Tente novamente em instantes.');}if(!result.accepted)throw new Error('Não foi possível salvar agora.');newsletter.reset();feedback.textContent='Cadastro recebido! Vamos avisar quando o OurTalk estiver disponível para download.';}catch(error){feedback.textContent=error.message||'Não foi possível salvar agora. Tente novamente.';}finally{button.disabled=false;}
});
