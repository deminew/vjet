vjo.ctype('vjoPro.samples.mtype.Employee9')
.mixin('vjoPro.samples.mtype.Person7')
.props({
gender : 'Male', //< public String

//> public String getGender()
getGender: function()
{
return this.gender;
}
})
.protos({
//> public void printGender()
printGender:function(){
//snippet.mixin.begin
//snippet.mixin.end
}
})
.endType();