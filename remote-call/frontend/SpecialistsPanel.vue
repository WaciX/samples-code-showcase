<template>
  <div class="q-pa-md">
    <div class="card-heading card-padding-dense">Manage Specialists</div>

    <q-list v-for="specialist in specialists" :key="specialist.id">
      <q-expansion-item
        label="First"
        dense
        expand-icon-class="specialist-head-outer"
        :modelValue="getToggle(specialist.id, true)"
        @update:modelValue="updateToggle(specialist.id, $event)"
      >
        <template v-slot:header>
          <SpecialistHeader
            :modelValue="specialist"
            :toggle="getToggle(specialist.id)"
          />
          <q-space></q-space>
        </template>

        <Specialist
          :modelValue="specialist"
          @update:modelValue="specialistUpdate"
        />

      </q-expansion-item>
    </q-list>


    <q-card>
      <q-item>
        <q-item-section>
          <q-item-label class="card-sub-heading card-padding-inner">Sub-accounts</q-item-label>
        </q-item-section>
      </q-item>

      <q-card-section horizontal>
        <q-card-section class="col-grow">
          <q-list dense>
            <q-item>
              <q-item-section class="card-text">Give your Customer a sub-account ID to let them remote into their own
                unattended PCs.
                <br />Unattended PCs used by sub-accounts count toward your unattended access quota.
              </q-item-section>
            </q-item>
          </q-list>
          <q-list v-for="specialistSubAccount in existingSubAccounts"
                  :key="specialistSubAccount.id"
          >
            <q-expansion-item
              group="specialistSubAccount"
              :default-opened="specialistSubAccount.newItem"
              label="First"
              dense
              expand-icon-class="specialist-head-outer"
              :modelValue="getToggle(specialistSubAccount.id, false)"
              @update:modelValue="updateToggle(specialistSubAccount.id, $event)"
            >
              <template v-slot:header>
                <SpecialistSubAccountHeader
                  :modelValue="specialistSubAccount"
                  :toggle="getToggle(specialistSubAccount.id)"
                  :newItem="false"
                />
                <q-space></q-space>
              </template>

              <SpecialistSubAccount
                :modelValue="specialistSubAccount"
                :newItem="false"
                @deleted="deleteSubAccount"
              />

            </q-expansion-item>
          </q-list>
          <q-list v-for="specialistSubAccount in newSpecialistsSubAccounts"
                  :key="specialistSubAccount.uuid"
          >
            <q-expansion-item
              group="specialistSubAccount"
              default-opened
              label="First"
              dense
              expand-icon-class="specialist-head-outer"
              :modelValue="true"
            >
              <template v-slot:header>
                <SpecialistSubAccountHeader
                  :modelValue="specialistSubAccount"
                  :toggle="true"
                  :newItem="true"
                />
                <q-space></q-space>
              </template>

              <SpecialistSubAccount
                :modelValue="specialistSubAccount"
                :newItem="true"
                @saved="saveSubAccount"
                @deleted="deleteNewSubAccount"
              />

            </q-expansion-item>
          </q-list>
        </q-card-section>
      </q-card-section>

      <q-item>
        <q-item-section>
          <q-item-label class="card-padding-inner">
            <q-btn flat no-caps label="Create sub-account" icon="add"
                   @click="addNewSpecialistSubAccount"
            ></q-btn>
          </q-item-label>
        </q-item-section>
      </q-item>
    </q-card>

  </div>
</template>

<script>
import { defineComponent, ref } from "vue";
import { mapWritableState } from "pinia/dist/pinia";
import { useStore } from "stores/ninja-forms-store";
import { v4 as uuidv4 } from "uuid";
import { Logger } from "src/utils/logger";

const logger = new Logger("SpecialistsPanel");

export default defineComponent({
  name: "SpecialistsPanel",
  data() {
    return {
      specialistsToggles: [],
      newSpecialistsSubAccounts: []
    };
  },
  computed: {
    ...mapWritableState(useStore, [
      "specialists",
      "specialistsSubAccounts"
    ]),
    existingSubAccounts() {
      return this.specialistsSubAccounts
        .filter(specialistSubAccount => specialistSubAccount["uuid"] === undefined);
    }
  },
  watch: {
    specialistsSubAccounts() {
      this.newSpecialistsSubAccounts = this.newSpecialistsSubAccounts
        .filter(newSpecialistsSubAccount => {
          const index = this.existingSubAccounts.findIndex(specialistSubAccount =>
            specialistSubAccount.id === newSpecialistsSubAccount.id);
          return index === -1;
        });
    }
  },
  methods: {
    updateToggle(id, value) {
      this.specialistsToggles[id] = value;
    },
    getToggle(id, defaultExpanded) {
      if (this.specialistsToggles[id] === undefined) {
        return defaultExpanded;
      }
      return this.specialistsToggles[id];
    },
    addNewSpecialistSubAccount() {
      this.newSpecialistsSubAccounts.push({
        "id": "",
        "name": "",
        "password": "",
        "uuid": uuidv4()
      });
    },
    deleteSubAccount(deletedSpecialistSubAccount) {
      logger.debug("Deleting sub account {0}", deletedSpecialistSubAccount);

      const index = this.specialistsSubAccounts.findIndex(specialistSubAccount =>
        specialistSubAccount.id === deletedSpecialistSubAccount.id);
      if (index === -1) {
        logger.warn("Sub account not found {0}", deletedSpecialistSubAccount);
        return;
      }
      this.specialistsSubAccounts.splice(index, 1);

      useStore().queueNextSave();
    },
    deleteNewSubAccount(deletedSpecialistSubAccount) {
      logger.debug("Deleting new sub account {0}", deletedSpecialistSubAccount);

      const index = this.newSpecialistsSubAccounts
        .findIndex(specialistSubAccount =>
          specialistSubAccount.uuid === deletedSpecialistSubAccount.uuid);
      if (index === -1) {
        return;
      }
      this.newSpecialistsSubAccounts.splice(index, 1);
    },
    saveSubAccount(newSubAccount) {
      logger.debug("Sub account save {0}", newSubAccount);

      const index = this.specialistsSubAccounts
        .findIndex(specialistSubAccount =>
          specialistSubAccount.uuid === newSubAccount.uuid);
      if (index !== -1) {
        this.specialistsSubAccounts.splice(index, 1);
      }
      this.specialistsSubAccounts.push(newSubAccount);
      useStore().queueNextSave();
    },
    specialistUpdate() {
      useStore().queueNextSave();
    }
  }
});
</script>

<style lang="sass">
.q-expansion-item__container > .q-item
  padding: 0

.specialist-head-outer
  position: absolute
  top: 40%
  right: 1%

.q-expansion-item__content > .q-card
  border-radius: 10px

.PhotoImageUpload
  display: flex
  flex-direction: column
  flex-wrap: nowrap
  align-content: center
  align-items: stretch

.PhotoImageUpload .q-img
  max-width: 128px
  max-height: 128px
  object-fit: contain
  align-self: center
</style>
