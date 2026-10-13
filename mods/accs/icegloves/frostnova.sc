0 : {
		stop();
		changeFace(2);
		fadeinAnimation( 250.0 , 250.0 , 250.0 , 4.5 , 0, 0.16) ;
		ignoreAttack(900);
		makeParticle("cold.ptc",0.0,0.0,0.0,0,1,0);
	}
400 : {
		jump(2.0);
		startAnimation( 250.0 , 252.0 , 252.0 , 10.0 , 0) ;
	}
500 : {
		makeShapeModel("/effect/iceshot_hadou.oa",0,0.0,0.6,0,0,1);
		makeShapeModel("/effect/iceshot_hadou.oa",0,0.0,-0.6,0,0,-1);
		makeShapeModel("/effect/iceshot_clash.oa",0.6,0.0,0.0,1,0,0);
		makeShapeModel("/effect/iceshot_clash.oa",-0.6,0.0,0.0,-1,0,0);
		makeShapeModel("/effect/frzflush.oa",0,0.2,0,0,1,0,1);
		attack("nova0");
		attack("nova90");
		attack("nova180");
		attack("nova270");
	}
700 : {
		makeShapeModel("/effect/frzflush.oa",0,0.3,0,0,1,0,1);
		startAnimation( 252.0 , 254.0 , 252.0 , 24 , 1) ;
	}
1300 : {
		recover();
	}
