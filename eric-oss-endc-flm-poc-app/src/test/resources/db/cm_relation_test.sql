--
-- COPYRIGHT Ericsson 2024
--
--
--
-- The copyright to the computer program(s) herein is the property of
--
-- Ericsson Inc. The programs may be used and/or copied only with written
--
-- permission from Ericsson Inc. or in accordance with the terms and
--
-- conditions stipulated in the agreement/contract under which the
--
-- program(s) have been supplied.
--

INSERT INTO ENODEBFUNCTION (ME_FDN,RES_REF,PARENT_RES_REF,ENBID,MCC,MNC,MNC_LENGTH,ENDC_ALLOWED) VALUES
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1','',730548,525,1,2,true);

INSERT INTO EUTRAN_CELL (DTYPE,ME_FDN,RES_REF,PARENT_RES_REF,CELL_ID) VALUES
    ('EUtranCellFDD','SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,EUtranCellFDD=73054815','ENodeBFunction=1',15),
    ('EUtranCellFDD','SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,EUtranCellFDD=73054816','ENodeBFunction=1',15);

INSERT INTO EXTERNALGNODEBFUNCTION (ME_FDN,RES_REF,PARENT_RES_REF,G_NODEBID,G_NODEBID_LENGTH,MCC,MNC,MNC_LENGTH) VALUES
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,GUtraNetwork=1,ExternalGNodeBFunction=5251-1037902','ENodeBFunction=1,GUtraNetwork=1',1037902,23,525,1,2);
INSERT INTO EXTERNALGUTRAN_CELL (ME_FDN,RES_REF,PARENT_RES_REF,LOCAL_CELL_ID) VALUES
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,GUtraNetwork=1,ExternalGNodeBFunction=5251-1037902,ExternalGUtranCell=5251-0000000001037902-1021','ENodeBFunction=1,GUtraNetwork=1,ExternalGNodeBFunction=5251-1037902',1021),
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,GUtraNetwork=1,ExternalGNodeBFunction=5251-1037902,ExternalGUtranCell=5251-0000000001037902-1022','ENodeBFunction=1,GUtraNetwork=1,ExternalGNodeBFunction=5251-1037902',1022),
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,GUtraNetwork=1,ExternalGNodeBFunction=5251-1037902,ExternalGUtranCell=5251-0000000001037902-1023','ENodeBFunction=1,GUtraNetwork=1,ExternalGNodeBFunction=5251-1037902',1023),
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,GUtraNetwork=1,ExternalGNodeBFunction=5251-1037902,ExternalGUtranCell=5251-0000000001037902-1024','ENodeBFunction=1,GUtraNetwork=1,ExternalGNodeBFunction=5251-1037902',1024);

INSERT INTO GUTRAN_FREQ_RELATION (ME_FDN,RES_REF,PARENT_RES_REF) VALUES
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,EUtranCellFDD=73054815,GUtranFreqRelation=426970-15-20-0-2','ENodeBFunction=1,EUtranCellFDD=73054815'),
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,EUtranCellFDD=73054816,GUtranFreqRelation=426970-15-20-0-3','ENodeBFunction=1,EUtranCellFDD=73054816');

INSERT INTO GUTRAN_CELL_RELATION (ME_FDN,RES_REF,PARENT_RES_REF,IS_ENDC_ALLOWED,NEIGHBOR_CELL_REF_ME_FDN,NEIGHBOR_CELL_REF_RES_REF) VALUES
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,EUtranCellFDD=73054815,GUtranFreqRelation=426970-15-20-0-2,GUtranCellRelation=5251-0000000001037902-1021','ENodeBFunction=1,EUtranCellFDD=73054815,GUtranFreqRelation=426970-15-20-0-2',true,'SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,GUtraNetwork=1,ExternalGNodeBFunction=5251-1037902,ExternalGUtranCell=5251-0000000001037902-1021'),
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,EUtranCellFDD=73054816,GUtranFreqRelation=426970-15-20-0-3,GUtranCellRelation=5251-0000000001037902-1022','ENodeBFunction=1,EUtranCellFDD=73054816,GUtranFreqRelation=426970-15-20-0-3',true,'SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,GUtraNetwork=1,ExternalGNodeBFunction=5251-1037902,ExternalGUtranCell=5251-0000000001037902-1022'),
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,EUtranCellFDD=73054816,GUtranFreqRelation=426970-15-20-0-3,GUtranCellRelation=5251-0000000001037902-1023','ENodeBFunction=1,EUtranCellFDD=73054816,GUtranFreqRelation=426970-15-20-0-3',true,'SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,GUtraNetwork=1,ExternalGNodeBFunction=5251-1037902,ExternalGUtranCell=5251-0000000001037902-1023'),
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,EUtranCellFDD=73054816,GUtranFreqRelation=426970-15-20-0-3,GUtranCellRelation=5251-0000000001037902-1024','ENodeBFunction=1,EUtranCellFDD=73054816,GUtranFreqRelation=426970-15-20-0-3',true,'SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,GUtraNetwork=1,ExternalGNodeBFunction=5251-1037902,ExternalGUtranCell=5251-0000000001037902-1024');

INSERT INTO GUTRAN_SYNC_SIGNAL_FREQUENCY (ME_FDN,RES_REF,PARENT_RES_REF,ARFCN) VALUES
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,GUtraNetwork=1,GUtranSyncSignalFrequency=630000-33','ENodeBFunction=1,GUtraNetwork=1',33),
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','ENodeBFunction=1,GUtraNetwork=1,GUtranSyncSignalFrequency=630000-44','ENodeBFunction=1,GUtraNetwork=1',44);

INSERT INTO GNBCUCPFUNCTION (ME_FDN,RES_REF,PARENT_RES_REF,GNBID,MCC,MNC,MNC_LENGTH) VALUES
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','GNBCUCPFunction=1','',1037902,525,1,2),
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','GNBCUCPFunction=2','',1037903,525,1,2);

INSERT INTO NRCELLCU (ME_FDN,RES_REF,PARENT_RES_REF,CELL_LOCAL_ID,MCC,MNC,MNC_LENGTH,PSCELL_CAPABLE) VALUES
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-1','GNBCUCPFunction=1',1021,525,1,2, true),
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-2','GNBCUCPFunction=1',1022,525,1,2, true),
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-3','GNBCUCPFunction=1',1023,525,1,2, true),
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-4','GNBCUCPFunction=1',1024,525,1,2, true),
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','GNBCUCPFunction=2,NRCellCU=NR03gNodeBRadio00002-1','GNBCUCPFunction=2',990,525,1,2, false);

INSERT INTO EXTERNALNRCELLCU (ME_FDN,RES_REF,PARENT_RES_REF,CELL_LOCAL_ID) VALUES
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','GNBCUCPFunction=2,NRNetwork=1,ExternalGNBCUCPFunction=auto310_260_3_1597264,ExternalNRCellCU=auto6542393345','GNBCUCPFunction=2,NRNetwork=1,ExternalGNBCUCPFunction=auto310_260_3_1597264',990);

INSERT INTO EXTERNALGNBCUCPFUNCTION (ME_FDN,RES_REF,PARENT_RES_REF,GNBID,MCC,MNC,MNC_LENGTH) VALUES
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','GNBCUCPFunction=2,NRNetwork=1,ExternalGNBCUCPFunction=auto310_260_3_1597264','GNBCUCPFunction=2,NRNetwork=1',1037903,525,1,2);

INSERT INTO NRCELL_RELATION (ME_FDN,RES_REF,PARENT_RES_REF,COVERAGE_INDICATOR,S_CELL_CANDIDATE,NRCELL_ME_FDN,NRCELL_RES_REF) VALUES
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-1,NRFreqRelation=1','GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-1',1,1,'SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-2'),
    ('SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-1,NRFreqRelation=2','GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-1',1,1,'SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building','GNBCUCPFunction=2,NRNetwork=1,ExternalGNBCUCPFunction=auto310_260_3_1597264,ExternalNRCellCU=auto6542393345');
