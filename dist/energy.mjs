export function sizeEnergy(input) {
 const number=(value,min,max)=>{if(typeof value!=='number'||!Number.isFinite(value)||value<min||value>max)throw new RangeError('invalid_input');return value;};
 const days=number(input.days,1,30),reserve=number(input.reserve,0,1),efficiency=number(input.efficiency,.1,1),usable=number(input.usable,.1,1),sunHours=number(input.sunHours,0,12),solarYield=number(input.solarYield,.1,1),recoveryDays=number(input.recoveryDays,1,30),voltage=number(input.voltage,1,1000);
 if(!Array.isArray(input.loads)||input.loads.length>30)throw new RangeError('invalid_loads');
 let dailyWh=0,acWatts=0,dcWatts=0;
 const loads=input.loads.map(load=>{const count=number(load.count,0,100);if(!Number.isInteger(count))throw new RangeError('invalid_count');const amount=number(load.amount,0,100000);const usage=number(load.usage,0,24);if(!['watts','charge'].includes(load.mode))throw new RangeError('invalid_mode');const daily=amount*usage*count;const power=load.mode==='charge'?number(load.chargeWatts,0,1000):amount;if(count>0&&usage>0){if(load.ac)acWatts+=power*count;else dcWatts+=power*count;}dailyWh+=daily;return {...load,dailyWh:daily};});
 const idleWatts=number(input.idleWatts??0,0,10000),idleHours=number(input.idleHours??0,0,24);
 dailyWh+=idleWatts*idleHours;
 const outputWh=dailyWh*days;
 const storedDailyWh=dailyWh*(1+reserve)/efficiency;
 const storageWh=storedDailyWh*days;
 const batteryWh=storageWh/usable;
 const solarDailyW=sunHours>0?storedDailyWh/(sunHours*solarYield):null;
 const solarRecoveryW=sunHours>0?(storedDailyWh+storageWh/recoveryDays)/(sunHours*solarYield):null;
 const inverterContinuousW=acWatts===0?0:acWatts*1.25;
 const startupW=number(input.startupWatts,0,1000000);
 return {loads,dailyWh,outputWh,batteryWh,batteryAh:batteryWh/voltage,acWatts,dcWatts,inverterContinuousW,inverterSurgeW:Math.max(acWatts,startupW),solarDailyW,solarRecoveryW,storageWh};
}
