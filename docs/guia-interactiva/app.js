/* Simulación educativa: todo el estado vive en esta página, sin peticiones a la API real. */
(() => {
  'use strict';
  const data=window.GUIDE_DATA;
  const $=id=>document.getElementById(id);
  const products={mouse:{id:1,name:'Mouse inalámbrico',cents:9500},keyboard:{id:2,name:'Teclado USB',cents:12000},stand:{id:3,name:'Base para laptop',cents:17500}};
  const state={step:0,scenario:'normal',running:false,saved:false,error:null,order:null,desktopLoaded:false};
  let timer=null,animation=0,paths={},quizIndex=0,quizAnswered=false,quizScore=0,lastComponentButton=null;
  const money=cents=>'Q'+(cents/100).toLocaleString('es-GT',{minimumFractionDigits:2,maximumFractionDigits:2});
  const reduced=()=>window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  const selection=()=>({customerId:Number($('customer').value),customer:$('customer').selectedOptions[0].textContent,product:products[$('product').value],quantity:Number($('quantity').value)});
  const valid=()=>{const q=selection().quantity;return Number.isSafeInteger(q)&&q>=1&&q<=20;};
  function totals(){const s=selection();const sub=s.product.cents*s.quantity;return {sub,iva:Math.round(sub*.12),total:sub+Math.round(sub*.12)};}
  function requestJson(){const s=selection();return JSON.stringify({clienteId:s.customerId,observaciones:'Simulación educativa',detalles:[{productoId:s.product.id,cantidad:s.quantity}]},null,2);}
  function responseJson(){return JSON.stringify({id:123,numeroOrden:'DEMO-001',estado:'PENDIENTE',total:(totals().total/100).toFixed(2)},null,2);}
  function errorFor(step){
    if(step>=1&&!valid())return {title:'Android detiene un dato inválido',text:'La cantidad debe ser un entero entre 1 y 20 en este ejemplo. El formulario muestra el error y no envía la orden. La API también valida los datos que recibe.',active:'android',code:'No se envía POST.\nCorrige la cantidad antes de guardar.'};
    if(state.scenario==='ngrok'&&step>=2)return {title:'El puente público está detenido',text:'La petición no llega a la API. Android informa del fallo de conexión; la computadora no recibe esta orden y SQL Server no guarda nada nuevo.',active:'cloud',code:'Sin respuesta del túnel.\nNo hay confirmación de creación.\nNo se guardó esta orden simulada.'};
    if(state.scenario==='sql'&&step>=5)return {title:'La base no puede guardar la orden',text:'La API recibió la solicitud, pero SQL Server no está disponible. Devuelve un error de servicio; Android lo muestra y conserva los datos para corregir el problema.',active:'sql',code:'HTTP/1.1 503 Service Unavailable\n\n{ "codigo": "BASE_NO_DISPONIBLE",\n  "mensaje": "La base de datos no está disponible. Intente nuevamente." }\n\nNo se muestra una confirmación de éxito.'};
    return null;
  }
  function stop(){clearTimeout(timer);timer=null;state.running=false;}
  function reset(){stop();cancelAnimationFrame(animation);$('travel-packet').style.display='none';if(state.scenario==='invalid'&&valid())state.scenario='normal';Object.assign(state,{step:0,saved:false,error:null,order:null,desktopLoaded:false});render();}
  function currentCode(){
    if(state.error)return state.error.code;
    if(state.step===0){const s=selection();return `Cliente: ${s.customer}\nProducto: ${s.product.name}\nCantidad: ${s.quantity}\nPrecio unitario: ${money(s.product.cents)}\n\nEjemplo ficticio; los cambios se quedan en esta página.`;}
    if(state.step===1)return requestJson();
    if(state.step===2)return 'POST https://tu-tunel.ngrok.example/api/v1/ordenes-venta\nContent-Type: application/json\n\n'+requestJson()+'\n\nDirección ilustrativa: en la app se configura la URL real de ngrok.';
    if(state.step===4){const t=totals();return 'OrdenesController\n  → OrdenesApiService\n  → OrdenVentaService\n  → OrdenVentaDAOImpl\n\nSubtotal: '+money(t.sub)+'\nIVA: '+money(t.iva)+'\nTotal: '+money(t.total);}
    if(state.step===6)return 'HTTP/1.1 201 Created\nContent-Type: application/json\nLocation: /api/v1/ordenes-venta/123\n\n'+responseJson();
    if(state.step===8)return responseJson();
    return data.steps[state.step].code;
  }
  function renderDesktop(){
    const show=state.desktopLoaded&&state.saved&&state.order;
    $('desktop-receipt').hidden=!show;$('desktop-empty').hidden=!!show;
    if(show){const o=state.order;$('receipt-number').textContent=o.number;$('receipt-customer').textContent=o.customer;$('receipt-product').textContent=o.quantity+' × '+o.product;$('receipt-total').textContent=money(o.total);}
    if(!state.desktopLoaded)$('desktop-feedback').textContent='';
  }
  function render(){
    const step=data.steps[state.step],current=state.error||step;
    const qOk=valid(),t=qOk?totals():null;
    $('subtotal').textContent=t?money(t.sub):'—';$('iva').textContent=t?money(t.iva):'—';$('total').textContent=t?money(t.total):'—';
    $('quantity-error').hidden=qOk;$('quantity-error').textContent=qOk?'':'Usa un entero entre 1 y 20.';$('quantity').setAttribute('aria-invalid',String(!qOk));
    $('step-count').textContent=state.step===0?'Listo para comenzar':(state.error?'Recorrido interrumpido · ':'')+'Paso '+state.step+' de 8';
    $('step-number').textContent=String(state.step).padStart(2,'0');$('step-title').textContent=current.title;$('step-text').textContent=current.text;
    $('message-title').textContent=state.error?'El resultado del fallo':step.codeTitle;$('message-code').textContent=currentCode();
    $('previous').disabled=state.step===0;$('next').disabled=!!state.error||state.step===8;
    $('play').disabled=!!state.error;$('play-label').textContent=state.running?'Pausar':state.step===8?'Repetir recorrido':state.step===0?'Iniciar recorrido':'Continuar';
    $('play-icon').querySelector('use').setAttribute('href',state.running?'#i-pause':'#i-play');
    $('tour-progress-fill').style.width=(state.step/8*100)+'%';
    document.querySelectorAll('[id^="node-"]').forEach(node=>{const active=node.id==='node-'+current.active;node.dataset.active=String(active);node.classList.toggle('is-active',active);node.classList.toggle('is-error',!!state.error&&active);node.classList.toggle('is-saved',node.id==='node-sql'&&state.saved);});
    $('db-state').textContent=state.saved?'DEMO-001 · guardada':state.error?.active==='sql'?'No disponible':'Sin orden nueva';
    const result=$('phone-result');result.classList.toggle('success',state.step===8&&!state.error);result.classList.toggle('error',!!state.error);
    result.textContent=state.error?'No se creó la orden. '+(state.error.active==='android'?'Revisa la cantidad.':'Revisa la conexión antes de reintentar.'):state.step===8?'Orden DEMO-001 creada · '+money(t.total):state.step>0?'Siguiendo los datos de tu orden…':'Ningún dato sale de esta página.';
    $('simulate').textContent=state.step===8?'Simular otra vez':'Simular guardar orden';
    $('journey-map').dataset.state=state.error?'error':state.step===8?'complete':'step-'+state.step;
    for(const id of ['customer','product','quantity','qty-minus','qty-plus'])$(id).disabled=state.running;
    document.querySelectorAll('[data-scenario]').forEach(b=>{const is=b.dataset.scenario===state.scenario;b.classList.toggle('active',is);b.setAttribute('aria-pressed',String(is));});
    const hints={normal:'El camino está disponible. Prueba el recorrido y después cambia una condición.',ngrok:'El servicio público no podrá entregar el mensaje a la computadora. Inicia el recorrido para localizar el fallo.',sql:'El teléfono y la API pueden hablar, pero la base no podrá guardar. Sigue el recorrido hasta SQL.',invalid:'La cantidad es cero. Observa cómo Android impide el envío; después corrígela o cambia el escenario.'};
    $('scenario-hint').textContent=hints[state.scenario];
    renderDesktop();markPaths();
  }
  function go(step,animate=true){
    state.step=Math.max(0,Math.min(8,step));state.error=errorFor(state.step);
    state.saved=state.step>=5&&!state.error&&state.scenario==='normal';
    if(state.saved){const s=selection();state.order={number:'DEMO-001',customer:s.customer,product:s.product.name,quantity:s.quantity,total:totals().total};}else{state.order=null;state.desktopLoaded=false;}
    if(state.error||state.step===8)stop();render();if(animate)animatePaths();
  }
  function schedule(){
    clearTimeout(timer);if(!state.running)return;
    timer=setTimeout(()=>{go(state.step+1);if(state.running)schedule();},7500);
  }
  function start(){stop();if(state.step===8||state.error)reset();if(state.step===0)go(1);if(!state.error&&state.step<8){state.running=true;render();schedule();}}
  $('simulate').addEventListener('click',()=>{reset();start();});
  $('play').addEventListener('click',()=>{if(state.running){stop();render();}else start();});
  $('previous').addEventListener('click',()=>{stop();go(state.step-1);});
  $('next').addEventListener('click',()=>{stop();if(!state.error)go(state.step+1);});
  $('reset').addEventListener('click',reset);
  for(const id of ['customer','product','quantity'])$(id).addEventListener('change',reset);
  $('quantity').addEventListener('input',()=>{if(!state.running)reset();});
  $('qty-minus').addEventListener('click',()=>{$('quantity').value=Math.max(0,(Number($('quantity').value)||0)-1);reset();});
  $('qty-plus').addEventListener('click',()=>{$('quantity').value=Math.min(20,(Number($('quantity').value)||0)+1);reset();});
  document.querySelectorAll('[data-scenario]').forEach(b=>b.addEventListener('click',()=>{state.scenario=b.dataset.scenario;if(state.scenario==='invalid')$('quantity').value=0;else if(!valid())$('quantity').value=2;reset();}));
  $('code-toggle').addEventListener('click',()=>{const open=$('message-inspector').hidden;$('message-inspector').hidden=!open;$('code-toggle').setAttribute('aria-expanded',String(open));});

  function component(id,button){
    const c=data.components[id];if(!c)return;lastComponentButton=button;
    $('component-where').textContent=c.where;$('component-title').textContent=c.label;$('component-role').textContent=c.role;$('component-input').textContent=c.input;$('component-output').textContent=c.output;
    $('component-layer-list').replaceChildren();for(const l of c.layers){const li=document.createElement('li');const strong=document.createElement('strong');strong.textContent=l.name;li.append(strong,document.createTextNode(' — '+l.description));$('component-layer-list').append(li);}
    $('component-layers').open=false;$('component-drawer').hidden=false;$('component-title').setAttribute('tabindex','-1');$('component-title').focus({preventScroll:true});$('component-drawer').scrollIntoView({behavior:reduced()?'auto':'smooth',block:'nearest'});
  }
  document.querySelectorAll('[data-component]').forEach(b=>b.addEventListener('click',()=>component(b.dataset.component,b)));
  $('component-close').addEventListener('click',()=>{$('component-drawer').hidden=true;lastComponentButton?.focus({preventScroll:true});});
  function layers(id){
    const c=data.components[id];$('layer-stack').replaceChildren();
    for(const [i,l] of c.layers.entries()){const li=document.createElement('li');const n=document.createElement('span');n.className='layer-number';n.textContent=String(i+1).padStart(2,'0');const div=document.createElement('div');const h=document.createElement('h3');h.textContent=l.name;const p=document.createElement('p');p.textContent=l.description;div.append(h,p);li.append(n,div);$('layer-stack').append(li);}
    $('architecture-note').textContent=id==='android'?'Todas estas capas corren dentro de la app del teléfono.':id==='api'?'Estas capas corren dentro de Spring Boot en tu computadora.':'Estas capas corren dentro del programa Swing en tu computadora.';
    document.querySelectorAll('[data-layers]').forEach(b=>{const selected=b.dataset.layers===id;b.classList.toggle('active',selected);b.setAttribute('aria-pressed',String(selected));});
  }
  document.querySelectorAll('[data-layers]').forEach(b=>b.addEventListener('click',()=>layers(b.dataset.layers)));
  $('desktop-query').addEventListener('click',()=>{
    if(state.scenario==='sql'){$('desktop-feedback').textContent='SQL Server no está disponible. Escritorio también necesita la base para consultar.';return;}
    if(!state.saved){$('desktop-feedback').textContent='Aún no existe una orden nueva en esta simulación. Completa primero el guardado desde Android.';return;}
    state.desktopLoaded=true;renderDesktop();$('desktop-feedback').textContent='Consulta completada: es la misma orden que guardó Android.';animatePaths([{id:'desktop-sql',reverse:false},{id:'desktop-sql',reverse:true}]);
  });

  function renderQuiz(){
    const q=data.quiz[quizIndex];quizAnswered=false;$('quiz-count').textContent='PREGUNTA '+(quizIndex+1)+' DE '+data.quiz.length;$('quiz-question').textContent=q.question;$('quiz-options').replaceChildren();$('quiz-feedback').textContent='';$('quiz-feedback').className='quiz-feedback';$('quiz-next').hidden=true;
    q.options.forEach((label,index)=>{const b=document.createElement('button');b.type='button';b.className='quiz-option';const letter=document.createElement('span');letter.textContent=String.fromCharCode(65+index);b.append(letter,document.createTextNode(label));b.addEventListener('click',()=>{
      if(quizAnswered)return;quizAnswered=true;const ok=index===q.correct;if(ok)quizScore++;b.classList.add(ok?'correct':'incorrect');
      [...$('quiz-options').children].forEach((item,n)=>{item.disabled=true;if(n===q.correct)item.classList.add('correct');});
      $('quiz-feedback').textContent=(ok?'Exacto. ':'Casi. ')+q.explanation;$('quiz-feedback').classList.add(ok?'success':'review');$('quiz-next').hidden=false;$('quiz-next').textContent=quizIndex===data.quiz.length-1?'Ver mi resumen →':'Siguiente pregunta →';
    });$('quiz-options').append(b);});
  }
  $('quiz-next').addEventListener('click',()=>{
    if(!quizAnswered)return;
    if(quizIndex<data.quiz.length-1){quizIndex++;renderQuiz();return;}
    $('quiz-count').textContent='RECORRIDO COMPLETADO';$('quiz-question').textContent='Ya puedes contar qué hace cada parte.';$('quiz-options').replaceChildren();$('quiz-feedback').textContent=`Acertaste ${quizScore} de ${data.quiz.length}. La idea central: Android presenta, ngrok conecta, la API procesa y SQL conserva.`;$('quiz-next').hidden=true;
    const again=document.createElement('button');again.type='button';again.className='button button-secondary';again.textContent='Practicar otra vez';again.addEventListener('click',()=>{quizIndex=0;quizScore=0;renderQuiz();});$('quiz-options').append(again);
  });
  $('menu-toggle').addEventListener('click',()=>{const open=$('menu-toggle').getAttribute('aria-expanded')!=='true';$('menu-toggle').setAttribute('aria-expanded',String(open));$('site-nav').classList.toggle('is-open',open);});
  $('site-nav').querySelectorAll('a').forEach(a=>a.addEventListener('click',()=>{$('site-nav').classList.remove('is-open');$('menu-toggle').setAttribute('aria-expanded','false');}));
  document.addEventListener('keydown',e=>{if(e.key==='Escape'){if(!$('component-drawer').hidden)$('component-close').click();$('site-nav').classList.remove('is-open');$('menu-toggle').setAttribute('aria-expanded','false');}});

  // Flechas calculadas a partir de las posiciones reales: se adaptan al cambio de pantalla.
  const map=$('journey-map'),svg=$('route-layer'),ns='http://www.w3.org/2000/svg';
  function svgElement(tag,attrs){const e=document.createElementNS(ns,tag);for(const [k,v] of Object.entries(attrs))e.setAttribute(k,v);return e;}
  function drawPaths(){
    const area=map.getBoundingClientRect();if(!area.width)return;
    cancelAnimationFrame(animation);$('travel-packet').style.display='none';svg.setAttribute('viewBox',`0 0 ${area.width} ${area.height}`);$('route-paths').replaceChildren();$('route-labels').replaceChildren();paths={};
    const box=id=>{const b=$('node-'+id).getBoundingClientRect();return {left:b.left-area.left,right:b.right-area.left,top:b.top-area.top,bottom:b.bottom-area.top,x:(b.left+b.right)/2-area.left,y:(b.top+b.bottom)/2-area.top};};
    const edges=[['phone-cloud','android','cloud','POST · JSON'],['cloud-agent','cloud','agent','Túnel seguro'],['agent-api','agent','api','HTTP local'],['api-sql','api','sql','JDBC / SQL'],['desktop-sql','desktop','sql','JDBC directo']];
    for(const [id,from,to,label] of edges){
      const a=box(from),b=box(to);let d,lx,ly,anchor='middle';const mobile=box('cloud').top>=box('android').bottom;
      if(id==='phone-cloud'&&!mobile){const gutter=a.right+26;d=`M ${a.right} ${a.y} L ${gutter} ${a.y} L ${gutter} ${b.y} L ${b.left} ${b.y}`;lx=gutter+12;ly=b.y-12;anchor='start';}
      else if(id==='cloud-agent'){
        const mid=a.bottom+22;
        if(mobile){const laptop=document.querySelector('.laptop-region').getBoundingClientRect();const gutter=laptop.left-area.left+7;d=`M ${a.x} ${a.bottom} L ${a.x} ${mid} L ${gutter} ${mid} L ${gutter} ${b.y} L ${b.left} ${b.y}`;lx=(a.x+gutter)/2;}
        else {d=`M ${a.x} ${a.bottom} L ${a.x} ${mid} L ${b.x} ${mid} L ${b.x} ${b.top}`;lx=(a.x+b.x)/2;}
        ly=mid-5;
      }
      else if(id==='api-sql'||(id==='phone-cloud'&&mobile)){d=`M ${a.x} ${a.bottom} L ${a.x} ${(a.bottom+b.top)/2} L ${b.x} ${(a.bottom+b.top)/2} L ${b.x} ${b.top}`;lx=a.x-10;ly=(a.bottom+b.top)/2-6;anchor='end';}
      else{d=`M ${a.right} ${a.y} L ${(a.right+b.left)/2} ${a.y} L ${(a.right+b.left)/2} ${b.y} L ${b.left} ${b.y}`;lx=(a.right+b.left)/2;ly=Math.min(a.top,b.top)-9;}
      const path=svgElement('path',{d,'data-route':id,'marker-end':'url(#route-arrow)'});$('route-paths').append(path);paths[id]=path;
      const text=svgElement('text',{x:lx,y:ly,'text-anchor':anchor});text.textContent=label;$('route-labels').append(text);
    }
    markPaths();
  }
  function markPaths(){const route=data.steps[state.step].paths;Object.entries(paths).forEach(([id,p])=>{p.classList.toggle('is-active',route.some(r=>r.id===id));p.classList.toggle('is-error',!!state.error&&route.some(r=>r.id===id));});}
  function animatePaths(custom){
    cancelAnimationFrame(animation);const packet=$('travel-packet');packet.style.display='none';if(reduced())return;
    const route=(custom||data.steps[state.step].paths).filter(r=>paths[r.id]);if(!route.length)return;let index=0,start=null;
    packet.classList.toggle('is-error',!!state.error);
    function tick(now){if(!route[index]){packet.style.display='none';return;}if(start===null)start=now;const r=route[index],path=paths[r.id],progress=Math.min(1,(now-start)/850),p=path.getPointAtLength(path.getTotalLength()*(r.reverse?1-progress:progress));packet.style.display='';packet.setAttribute('cx',p.x);packet.setAttribute('cy',p.y);if(progress>=1){index++;start=null;}animation=requestAnimationFrame(tick);}
    animation=requestAnimationFrame(tick);
  }
  new ResizeObserver(drawPaths).observe(map);
  document.addEventListener('visibilitychange',()=>{if(document.hidden){stop();render();cancelAnimationFrame(animation);$('travel-packet').style.display='none';}});
  window.VentasGuide=Object.freeze({getState:()=>JSON.parse(JSON.stringify(state))});
  layers('android');renderQuiz();render();requestAnimationFrame(drawPaths);
})();
