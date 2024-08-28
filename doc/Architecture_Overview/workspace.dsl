workspace {

    model {
        user = person "Operator"

        endcFLM = softwareSystem "ENDC FLM rApp" {

            owl = group "ENDC FLM rApp frontend" {

                owlFrontendService = container "OWL frontend" {
                    tags "Service"
                }

                owlBackendService = container "OWL backend" {
                    tags "Service"
                }
            }

            rappBackend = group "ENDC FLM rApp Backend" {

                springBootBackend = container "ENDC FLM SpringBoot Backend" {
                    cmService = component "CM Service" {
                        tags "Service"
                    }

                    topologyService = component "Topology Service" {
                        tags "Service"
                    }

                    topologyClient = component "Topology Client"
                    ncmpClient = component "NCMP Client"
                    pmCounterListener = component "PM Counter Listener"
                }

                endcFLMDatabase = container "PostgreSQL" {
                    tags "Database"
                }
            }
        }

        eic = softwareSystem "Ericsson Intelligent Controller" {
            dmm = group "DMM" {
                kafka = container "Kafka" {
                    tags "Bus"
                }
                dcc = container "DCC" {
                    tags "Service"
                }
            }

            tic = group "TIC" {
                ncmp = container "NCMP" {
                    tags "Service"
                }
                cts = container "CTS" {
                    tags "Service"
                }
            }
            bdr = container "BDR" {
                tags "Service"
            }
            kpiCalculation = container "KPI Calculation" {
                tags "Service"
            }

        }

        user -> owlFrontendService "[http over TLS] Upload/Retrive a list of allowed cells and nodes "

        owlFrontendService -> owlBackendService "[http over TLS] report on events, set up subscriptions"
        owlBackendService -> topologyService "[REST, unsecured] Retrieve eNodebs list"
        owlBackendService -> cmService "[REST, unsecured] Upload/Retrieve cells and eNodebs list"
        cmService -> endcFLMDatabase "[JDBC over TLS] retrieve CM data"
        topologyService -> topologyClient
        topologyService -> ncmpClient "[Java API] Matching Managed Object ID"
        topologyClient -> cts
        ncmpClient -> ncmp "[REST, http over TLS] featch CM information"
        ncmpClient -> endcFLMDatabase "[JDBC over TLS] cache CM data"
        pmCounterListener -> endcFLMDatabase "[JDBC over TLS] write PM Counter data"
        pmCounterListener -> kafka "[Kafka unsecured] listen to NR & LTE PM Counters"

        springBootBackend -> dcc
        springBootBackend -> bdr
        springBootBackend -> kpiCalculation

    }

    views {

        systemContext endcFLM "SystemView" {
            include *
        }

        container endcFLM "EndcFlmContainerView" {
            include *
        }

        container eic "EICContainerView" {
            include *
        }

        component springBootBackend "BackendComponentView" {
            include *
        }


        styles {
            element "Person" {
                shape person
                background #000082
                color #ffffff
            }
            element "SoftwareSystem" {
                background #000082
                color #ffffff
            }
            element "Container" {
                background #0050ca
                color #ffffff
            }
            element "Component" {
                background #1174e6
                color #ffffff
            }
            element "Service" {
                shape roundedBox
            }
            element "Listener" {
                shape box
            }
            element "Database" {
                shape cylinder
            }
            element "Bus" {
                shape pipe
            }
        }
    }
}