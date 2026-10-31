import fs from 'node:fs/promises';
import path from 'node:path';
import {createRequire} from 'node:module';
import {fileURLToPath} from 'node:url';
const require=createRequire(import.meta.url);
const sharp=require(process.env.SHARP_MODULE || 'sharp');
const root=path.dirname(fileURLToPath(import.meta.url));
const config=JSON.parse(await fs.readFile(path.join(root,'studio.json'),'utf8'));
const staging=path.join(root,'play-staging');
const limits={title:30,shortDescription:80,fullDescription:4000};
const listings=[];
for(const locale of config.locales){
 const listing=JSON.parse(await fs.readFile(path.join(root,locale,'listing.json'),'utf8'));
 for(const [key,max] of Object.entries(limits))if(!listing[key]?.trim() || [...listing[key]].length>max)throw Error(`${locale}: invalid ${key}`);
 listings.push({locale,listing});
}
const assets=JSON.parse(await fs.readFile(path.join(root,'en-US/assets.json'),'utf8'));
for(const [kind,type]of Object.entries({phone:'phoneScreenshots',tablet7:'sevenInchScreenshots',tablet10:'tenInchScreenshots'})){
 const images=assets.galleries[kind];
 if(images.length<2 || images.length>8)throw Error(`Invalid ${kind} screenshot count`);
 for(const [i,img]of images.entries()){
  const file=path.join(root,'en-US',img.file),meta=await sharp(file).metadata();
  if(!meta.width||!meta.height)throw Error(`Invalid image ${file}`);
  const out=path.join(staging,'en-US/images',type);await fs.mkdir(out,{recursive:true});
  const width=kind==='tablet10'?1920:1080,height=kind==='tablet10'?1080:1920;
  await sharp(file).resize({width,height,fit:'contain',background:'#f8f2e5'}).jpeg({quality:96}).toFile(path.join(out,`${String(i+1).padStart(2,'0')}.jpg`));
 }
}
for(const [file,name,width,height]of [['brand/icon.png','icon.png',512,512],['en-US/feature-graphic.png','featureGraphic.png',1024,500]]){
 const src=path.join(root,file),meta=await sharp(src).metadata();if(meta.width!==width||meta.height!==height)throw Error(`Invalid ${file}`);
 const out=path.join(staging,'en-US/images');await fs.mkdir(out,{recursive:true});await fs.copyFile(src,path.join(out,name));
}
for(const {locale,listing}of listings){
 const out=path.join(staging,locale);await fs.mkdir(out,{recursive:true});
 for(const [key,file]of Object.entries({title:'title.txt',shortDescription:'short_description.txt',fullDescription:'full_description.txt'}))await fs.writeFile(path.join(out,file),listing[key]+'\n');
}
console.log(`Staged ${listings.length} translated listings; English icon, feature graphic and phone/tablet screenshots. Other languages inherit English graphics. No upload performed.`);
