def tempest_install(){
  common.openstack_ansible(
    playbook: "os-tempest-install.yml",
    path: "/opt/rpc-openstack/openstack-ansible/playbooks"
  )
}

def tempest_run(Map args){
  sh """#!/bin/bash
    utility_container="\$(lxc-ls |grep -m1 utility)"
    lxc-attach \
      -n \$utility_container \
      -- /opt/openstack_tempest_gate.sh \
      ${env.TEMPEST_TEST_SETS}
  """
}
/* if tempest install fails, don't bother trying to run or collect test results
 * however if runnig fails, we should still collect the failed results
 */
def tempest(){
  stage("Tempest"){
    tempest_install()
    try{
      tempest_run()
    } catch (e){
      print(e)
      throw(e)
    } finally{
      sh "cp /openstack/log/*utility*/**/*tempest*.xml . ||:"
      junit allowEmptyResults: true, testResults: '**/*tempest*.xml'
    } //finally
  } //stage
} //func

return this;