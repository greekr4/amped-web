0 : {
		bind(900);
		makeShapeModel("/effect/explode2.oa",0,-0.3,0,0,1,0);
		makeShapeModel("/effect/explode2.oa",0,0.3,0.6,0,1,0);
		makeShapeModel("/effect/quake.oa",0,-0.4,0,0,1,0);
		makeShapeModel("/effect/crash.oa",0,-0.1,0,0,1,0);
		makeShapeModel("/effect/explode1.oa",1.2,-0.3,0,1,0,0);
		makeShapeModel("/effect/explode1.oa",-1.2,-0.3,0,-1,0,0);
		makeShapeModel("/effect/explode1.oa",0,-0.3,1.2,0,0,1);
		makeShapeModel("/effect/explode1.oa",0,-0.3,-1.2,0,0,-1);
		makeShapeModel("/effect/honoo.oa",0.8,-0.2,0.8,1,0,1);
		makeShapeModel("/effect/honoo.oa",-0.8,-0.2,0.8,-1,0,1);
		makeShapeModel("/effect/honoo.oa",0.8,-0.2,-0.8,1,0,-1);
		makeShapeModel("/effect/honoo.oa",-0.8,-0.2,-0.8,-1,0,-1);
		attack("crash0");
		attack("crash90");
		attack("crash180");
		attack("crash270");
		play("/sound/5.sr",100,1);
	}
120 : {
		makeShapeModel("/effect/shock.oa",0,-0.2,0,0,1,0);
	}
150 : {
		move(0,0,0);
		stop();
		startAnimation( 336.0 , 337.0 , 337.0 , 6.0 , 0) ;
	}
400 : {
		makeShapeModel("/effect/honoo.oa",0,0.2,0,0,1,0,1);
		startAnimation( 338.0 , 339.0 , 339.0 , 4.0 , 0) ;
	}
800 : {
		recover();
	}
